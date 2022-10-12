// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package frc.robot.commands.groups;

import edu.wpi.first.wpilibj2.command.ParallelCommandGroup;
import frc.robot.commands.indexer.LoadBalls;
import frc.robot.commands.intake.SpinIntake;

// NOTE:  Consider using this command inline, rather than writing a subclass.  For more
// information, see:
// https://docs.wpilib.org/en/stable/docs/software/commandbased/convenience-features.html
public class AutoIntake extends ParallelCommandGroup {
  /** Creates a new AutoIntake. */
  public AutoIntake() {
    addCommands(
      new SpinIntake(12.0),
      new LoadBalls()
    );
  }
}