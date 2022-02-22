package frc.robot.subsystems.shooter;

import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import edu.wpi.first.wpilibj2.command.CommandScheduler;
import edu.wpi.first.wpilibj2.command.SubsystemBase;
import frc.robot.Constants;
import frc.robot.subsystems.sensors.Sensors;

public class Shooter extends SubsystemBase {

  private static Shooter instance = null;
  public static synchronized Shooter getInstance() {
    if (instance == null) instance = new Shooter();
    return instance;
  }

  Hood hood = new Hood(Constants.shooter.hood.MOTOR_ID);
  Wheel wheel = new Wheel(Constants.shooter.LEADER_ID, Constants.shooter.FOLLOWER_ID);
  Sensors sensors = Sensors.getInstance();

  /** Creates a new Shooter. */
  private Shooter() {
    CommandScheduler.getInstance().registerSubsystem(this);

    SmartDashboard.putNumber("Shooter Set Velocity", 0.0);
  }

  public void setVolts(double lowerVolts) {
    wheel.setVoltage(lowerVolts);
  }

  public void setVelocity(double rpm) {
    wheel.setVelocity(rpm);
  }

  public void setAngle(double angle) {
    hood.setPosition(angle);
  }

  public double getVelocity() {
    return wheel.getVelocity();
  }

  public double getAngle() {
    return hood.getPosition();
  }

  @Override
  public void periodic() {
    wheel.periodic();
  }

public boolean isReady() {
    return Math.abs(sensors.getTX()) < 2.0;
}
}
