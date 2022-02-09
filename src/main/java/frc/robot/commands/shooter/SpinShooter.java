package frc.robot.commands.shooter;

import java.util.function.DoubleSupplier;

import edu.wpi.first.wpilibj2.command.CommandBase;
import frc.robot.Constants.shooter;
import frc.robot.subsystems.shooter.LowerShooter;
import frc.robot.subsystems.shooter.Shooter;
import frc.robot.subsystems.shooter.UpperShooter;

public class SpinShooter extends CommandBase {

  Shooter shooter = Shooter.getInstance();

  DoubleSupplier velocity;

  /** Creates a new ShooterVelocity. */
  public SpinShooter(DoubleSupplier velocity) {
    addRequirements(shooter);

    this.velocity = velocity;
  }

  // Called when the command is initially scheduled.
  @Override
  public void initialize() {}

  // Called every time the scheduler runs while the command is scheduled.
  @Override
  public void execute() {
    double vel = velocity.getAsDouble();
    shooter.setVelocity(vel);
  }

  // Called once the command ends or is interrupted.
  @Override
  public void end(boolean interrupted) {
    shooter.setVolatge(0);
  }

  // Returns true when the command should end.
  @Override
  public boolean isFinished() {
    return false;
  }
}