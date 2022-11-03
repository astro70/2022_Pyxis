package frc.robot.subsystems.drive;

import edu.wpi.first.math.VecBuilder;
import edu.wpi.first.math.controller.PIDController;
import edu.wpi.first.math.estimator.SwerveDrivePoseEstimator;
import edu.wpi.first.math.filter.SlewRateLimiter;
import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.math.geometry.Rotation2d;
import edu.wpi.first.math.kinematics.ChassisSpeeds;
import edu.wpi.first.math.kinematics.SwerveDriveKinematics;
import edu.wpi.first.math.kinematics.SwerveModuleState;
import edu.wpi.first.math.util.Units;
import edu.wpi.first.wpilibj.Timer;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import edu.wpi.first.wpilibj2.command.CommandScheduler;
import edu.wpi.first.wpilibj2.command.SubsystemBase;
import frc.robot.Constants;
import frc.robot.subsystems.sensors.Sensors;
import frc.robot.utils.math.Derivative;
import java.util.function.BooleanSupplier;

public class Swerve extends SubsystemBase {

  private static Swerve instance = null;

  public static synchronized Swerve getInstance() {
    if (instance == null) instance = new Swerve();

    return instance;
  }

  private Module leftFront = new Module(4, 3, 22, Constants.drive.modules.M1_ZERO);
  private Module rightFront = new Module(2, 1, 21, Constants.drive.modules.M2_ZERO);
  private Module leftRear = new Module(6, 5, 23, "Pyxis CANivore", Constants.drive.modules.M3_ZERO);
  private Module rightRear =
      new Module(8, 7, 24, "Pyxis CANivore", Constants.drive.modules.M4_ZERO);

  private Sensors sensors = Sensors.getInstance();

  SwerveDriveKinematics kinematics =
      new SwerveDriveKinematics(
          Constants.drive.FrontLeftLocation,
          Constants.drive.FrontRightLocation,
          Constants.drive.BackLeftLocation,
          Constants.drive.BackRightLocation);

  private PIDController rotationPID = new PIDController(0.1, 0.0, 0.0);
  private PIDController trackingPID = new PIDController(1, 0.0, 0.3);

  private PIDController pitchCorrection = new PIDController(-0.15, 0.0, 0.01);
  private PIDController rollCorrection = new PIDController(-0.1, 0.0, 0.075);

  // private Odometry odometry = new Odometry(new Pose2d());

  private Derivative accel = new Derivative(0.0);
  private Derivative d = new Derivative(0.0);

  private SlewRateLimiter xLimiter = new SlewRateLimiter(2.5);
  private SlewRateLimiter yLimiter = new SlewRateLimiter(2.5);
  private SlewRateLimiter rotLimiter = new SlewRateLimiter(2.0);

  private Pose2d initPose = null;

  private SwerveDrivePoseEstimator odometry =
      new SwerveDrivePoseEstimator(
          sensors.getRotation(),
          new Pose2d(1.0, 0.0, new Rotation2d()),
          kinematics,
          VecBuilder.fill(0.15, 0.15, Units.degreesToRadians(15.0)),
          VecBuilder.fill(Units.degreesToRadians(0.05)),
          VecBuilder.fill(0.05, 0.05, 0.05),
          0.02);

  /** Creates a new Swerve. */
  private Swerve() {
    CommandScheduler.getInstance().registerSubsystem(this);
    rotationPID.enableContinuousInput(-180, 180);
    rotationPID.setTolerance(1.0);

    SmartDashboard.putNumber("kP", 0.0);
    SmartDashboard.putNumber("kI", 0.0);
    SmartDashboard.putNumber("kD", 0.0);
  }

  @Override
  public void periodic() {

    odometry.update(sensors.getRotation(), getModuleStates());
    if (sensors.getHasTarget() /* && d.getRate(sensors.getTX()) < 5*/) {
      odometry.addVisionMeasurement(sensors.getVisionPose(), Timer.getFPGATimestamp() - 0.02);

      if (initPose == null) initPose = getPose();
    }

    // odometry.update(sensors.getRotation(), getModuleStates());
  }

  public void smartDashboard() {
    SmartDashboard.putNumber("M1 (LF) Angle", leftFront.getAbsoluteAngle());
    SmartDashboard.putNumber("M2 (RF) Angle", rightFront.getAbsoluteAngle());
    SmartDashboard.putNumber("M3 (LR) Angle", leftRear.getAbsoluteAngle());
    SmartDashboard.putNumber("M4 (RR) Angle", rightRear.getAbsoluteAngle());
    SmartDashboard.putString("Chassis Speeds", getChassisSpeeds().toString());
    SmartDashboard.putString("Odometry", odometry.getEstimatedPosition().toString());

    SmartDashboard.putNumber("Falcon 1 Temp", leftFront.getAngleTemp());
    SmartDashboard.putNumber("Falcon 2 Temp", leftFront.getSpeedTemp());
    SmartDashboard.putNumber("Falcon 3 Temp", rightFront.getAngleTemp());
    SmartDashboard.putNumber("Falcon 4 Temp", rightFront.getSpeedTemp());
    SmartDashboard.putNumber("Falcon 5 Temp", leftRear.getAngleTemp());
    SmartDashboard.putNumber("Falcon 6 Temp", leftRear.getSpeedTemp());
    SmartDashboard.putNumber("Falcon 7 Temp", rightRear.getAngleTemp());
    SmartDashboard.putNumber("Falcon 8 Temp", rightRear.getSpeedTemp());
  }

  public void drive(double yInput, double xInput, double rotInput, BooleanSupplier tracking) {
    // yInput = yLimiter.calculate(yInput) * Constants.drive.MAX_VELOCITY;
    // xInput = xLimiter.calculate(xInput) * -Constants.drive.MAX_VELOCITY;
    // rotInput = rotLimiter.calculate(rotInput) * 2.0 * Math.PI;
    yInput = yInput * Constants.drive.MAX_VELOCITY;
    xInput = xInput * -Constants.drive.MAX_VELOCITY;
    rotInput = rotInput * 2.0 * Math.PI;

    if (rotInput != 0.0) {
      setRotSetpoint(-sensors.getRotation().getDegrees());
    } else if (yInput != 0 || xInput != 0) {
      double correction = rotationPID.calculate(-sensors.getRotation().getDegrees());
      rotInput = rotationPID.atSetpoint() ? 0.0 : correction;
    }

    if (tracking.getAsBoolean()) {
      rotInput = trackingPID.calculate(-sensors.getClosestBallAngle(), 0.0) / (2 * Math.PI);
    }

    double pitch = Math.abs(sensors.getPitch()) < 1.5 ? 0.0 : sensors.getPitch();
    double roll = Math.abs(sensors.getRoll()) < 1.5 ? 0.0 : sensors.getRoll();

    ChassisSpeeds result;

    result = ChassisSpeeds.fromFieldRelativeSpeeds(yInput, xInput, rotInput, sensors.getRotation());

    result =
        new ChassisSpeeds(
            result.vxMetersPerSecond - pitchCorrection.calculate(pitch, 0.0),
            result.vyMetersPerSecond + rollCorrection.calculate(roll, 0.0),
            result.omegaRadiansPerSecond);
    setChassisSpeeds(result);
  }

  public void zeroPower() {
    leftFront.zeroPower();
    rightFront.zeroPower();
    leftRear.zeroPower();
    rightRear.zeroPower();
  }

  public void setRotSetpoint(double setpoint) {
    rotationPID.setSetpoint(setpoint);
  }

  public void setTrackingPID(double kP, double kI, double kD) {
    trackingPID.setPID(kP, kI, kD);
  }

  public PIDController getTrackingPID() {
    return trackingPID;
  }

  public double getRotSetpoint() {
    return rotationPID.getSetpoint();
  }

  public void setChassisSpeeds(ChassisSpeeds speeds) {
    ChassisSpeeds newSpeeds =
        new ChassisSpeeds(
            speeds.vxMetersPerSecond, speeds.vyMetersPerSecond, speeds.omegaRadiansPerSecond);
    setModuleStates(kinematics.toSwerveModuleStates(newSpeeds));
  }

  public void setModuleStates(SwerveModuleState[] states) {
    SwerveDriveKinematics.desaturateWheelSpeeds(states, 4.75);

    SmartDashboard.putNumber("Left Front Set", states[0].angle.getDegrees());
    leftFront.setModuleState(
        new SwerveModuleState(states[0].speedMetersPerSecond, states[0].angle));
    rightFront.setModuleState(
        new SwerveModuleState(states[1].speedMetersPerSecond, states[1].angle));
    leftRear.setModuleState(new SwerveModuleState(states[2].speedMetersPerSecond, states[2].angle));
    rightRear.setModuleState(
        new SwerveModuleState(states[3].speedMetersPerSecond, states[3].angle));
  }

  public ChassisSpeeds getChassisSpeeds() {
    return kinematics.toChassisSpeeds(getModuleStates());
  }

  public double getVelocity() {
    ChassisSpeeds chassisSpeeds = getChassisSpeeds();

    return Math.sqrt(
        Math.pow(chassisSpeeds.vxMetersPerSecond, 2)
            + Math.pow(chassisSpeeds.vyMetersPerSecond, 2));
  }

  public SwerveModuleState[] getModuleStates() {
    return new SwerveModuleState[] {
      leftFront.getModuleState(),
      rightFront.getModuleState(),
      leftRear.getModuleState(),
      rightRear.getModuleState()
    };
  }

  public void setPose(Pose2d pose) {
    odometry.resetPosition(pose, pose.getRotation());
  }

  public Pose2d getPose() {
    return odometry.getEstimatedPosition();
  }

  public SwerveDriveKinematics getKinematics() {
    return kinematics;
  }

  public double getSpeed() {
    return Math.hypot(getChassisSpeeds().vxMetersPerSecond, getChassisSpeeds().vyMetersPerSecond);
  }

  public double getAccel() {
    return accel.getRate(getSpeed());
  }

  public Pose2d getInitPose() {
    return initPose;
  }
}
