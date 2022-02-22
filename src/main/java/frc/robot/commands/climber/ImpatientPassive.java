// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package frc.robot.commands.climber;

import edu.wpi.first.wpilibj2.command.InstantCommand;
import frc.robot.subsystems.climber.Climber;

// NOTE:  Consider using this command inline, rather than writing a subclass.  For more
// information, see:
// https://docs.wpilib.org/en/stable/docs/software/commandbased/convenience-features.html
public class ImpatientPassive extends InstantCommand {

  private Climber climber = Climber.getInstance();
  private boolean pushing;

  public ImpatientPassive(boolean pushing) {
    addRequirements(climber);
    this.pushing = pushing;
  }

  // Called when the command is initially scheduled.
  @Override
  public void initialize() {
    climber.setPassivePosition(pushing);
  }
}
