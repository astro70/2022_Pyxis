package frc.robot.utils.motors;

public interface CSPMotor {

    public void setInverted(boolean inverted);

    public void reset();

    public void setBrake(boolean braking);

    public void setRamp(double ramp);
    
    public void set(double percent);

    public void setVoltage(double volts);

    public void setEncoder(double position);

    public double get();

    public double getVoltage();

    public double getVelocity();

    public double getPosition();

    public double getTemperature();
}