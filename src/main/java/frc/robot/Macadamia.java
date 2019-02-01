/*----------------------------------------------------------------------------*/
/* Copyright (c) 2017-2018 FIRST. All Rights Reserved.                        */
/* Open Source Software - may be modified and shared by FRC teams. The code   */
/* must be accompanied by the FIRST BSD license file in the root directory of */
/* the project.                                                               */
/*----------------------------------------------------------------------------*/

package frc.robot;

import edu.wpi.first.wpilibj.TimedRobot;
import edu.wpi.first.wpilibj.smartdashboard.SendableChooser;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import edu.wpi.first.wpilibj.Talon;
import edu.wpi.first.wpilibj.drive.DifferentialDrive;
import edu.wpi.first.wpilibj.Ultrasonic;
import edu.wpi.first.wpilibj.XboxController;
import edu.wpi.first.wpilibj.GenericHID.Hand;
import edu.wpi.first.cameraserver.CameraServer;
import edu.wpi.first.wpilibj.Timer;

/**
 * The VM is configured to automatically run this class, and to call the
 * functions corresponding to each mode, as described in the TimedRobot
 * documentation. If you change the name of this class or the package after
 * creating this project, you must also update the build.gradle file in the
 * project.
 */
public class Macadamia extends TimedRobot {
  private static final String kDefaultAuto = "Default";
  private static final String kCustomAuto = "My Auto";
  private String m_autoSelected;
  private final SendableChooser<String> m_chooser = new SendableChooser<>();

  private Talon leftTalon, rightTalon;
  private DifferentialDrive drive;
  private Ultrasonic frontUltrasonic, rearUltrasonic;
  private XboxController xbox;
  private Timer timer;
  private boolean    frontSafetyStop, rearSafetyStop;


  /**
   * This function is run when the robot is first started up and should be
   * used for any initialization code.
   */
  @Override
  public void robotInit() {
    m_chooser.setDefaultOption("Default Auto", kDefaultAuto);
    m_chooser.addOption("My Auto", kCustomAuto);
    SmartDashboard.putData("Auto choices", m_chooser);

    //Configure Drive
    leftTalon = new Talon(RobotMap.leftMotor);
    rightTalon = new Talon(RobotMap.rightMotor);
    drive = new DifferentialDrive(leftTalon, rightTalon);

    //Configure Ultrasonic sensors on front and rear
    double distance;
    try {
      frontUltrasonic = new Ultrasonic(RobotMap.frontPing, RobotMap.frontEcho); // ping, echo
      frontUltrasonic.setAutomaticMode(true);
      frontUltrasonic.setEnabled(true);
      distance = frontUltrasonic.getRangeInches();
      distance = frontUltrasonic.getRangeInches();
      System.out.println("Front distance: " + distance);
    }
    catch(Exception e) {
      System.out.println("Could not create front ultrasonic sensor (ping: " + RobotMap.frontPing + " echo: " + RobotMap.frontEcho + ")");
    }

    try {
      rearUltrasonic = new Ultrasonic(RobotMap.rearPing, RobotMap.rearEcho); // ping, echo
      rearUltrasonic.setAutomaticMode(true);
      rearUltrasonic.setEnabled(true);
      distance = rearUltrasonic.getRangeInches();
      distance = rearUltrasonic.getRangeInches();
      System.out.println("Rear distance: " + distance);
    }
    catch(Exception e) {
      System.out.println("Could not create rear ultrasonic sensor (ping: " + RobotMap.rearPing + " echo: " + RobotMap.rearEcho + ")");
    }

    //Configure Joystick input
    xbox = new XboxController(0);

    //Configure Camera
    CameraServer.getInstance().startAutomaticCapture();

    //Instantiate timer
    timer = new Timer();
  }

  /**
   * This function is called every robot packet, no matter the mode. Use
   * this for items like diagnostics that you want ran during disabled,
   * autonomous, teleoperated and test.
   *
   * <p>This runs after the mode specific periodic functions, but before
   * LiveWindow and SmartDashboard integrated updating.
   */
  @Override
  public void robotPeriodic() {
  }

  /**
   * This autonomous (along with the chooser code above) shows how to select
   * between different autonomous modes using the dashboard. The sendable
   * chooser code works with the Java SmartDashboard. If you prefer the
   * LabVIEW Dashboard, remove all of the chooser code and uncomment the
   * getString line to get the auto name from the text box below the Gyro
   *
   * <p>You can add additional auto modes by adding additional comparisons to
   * the switch structure below with additional strings. If using the
   * SendableChooser make sure to add them to the chooser code above as well.
   */
  @Override
  public void autonomousInit() {
    m_autoSelected = m_chooser.getSelected();
    // m_autoSelected = SmartDashboard.getString("Auto Selector", kDefaultAuto);
    System.out.println("Auto selected: " + m_autoSelected);

    timer.reset();
    timer.start();
  }

  /**
   * This function is called periodically during autonomous.
   */
  @Override
  public void autonomousPeriodic() {
    if (timer.get() < 2.0) {
        drive.curvatureDrive(0.1, 0.0, false);
      }
      else
      {
        drive.curvatureDrive(0.0, 0.0,false);
      }
 
 
    switch (m_autoSelected) {
      case kCustomAuto:
        // Put custom auto code here
        break;
      case kDefaultAuto:
      default:
        // Put default auto code here
        break;
    }
  }

  /**
   * This function is called periodically during operator control.
   */
  @Override
  public void teleopPeriodic() {
    // Use controller joysticks to set drive speed, but
    // safety stop if too close to an obstacle
    double leftSpeed  = -0.5*xbox.getY(Hand.kLeft);
    double rightSpeed = -0.5*xbox.getY(Hand.kRight);

    // If there's an obstacle in front of us, don't
    // allow any more forward motion
    if (safetyStop(15.0, frontUltrasonic) && ((leftSpeed > 0.0) || (rightSpeed > 0.0))) {
       drive.stopMotor();
    } else if (safetyStop(15.0, rearUltrasonic) && ((leftSpeed < 0.0) || (rightSpeed < 0.0))) {
        drive.stopMotor();
    } else {
      // otherwise, set motors according to joysticks
       drive.tankDrive(leftSpeed, rightSpeed);
    }
    Timer.delay(0.01);
}

  /**
   * This function is called periodically during test mode.
   */
  @Override
  public void testPeriodic() {
  }

    // Use  ultrasonic sensor to stop robot
    // if it gets too close to an obstacle 
    public boolean safetyStop(double safeDistance, Ultrasonic sensor) {
    double distance = sensor.getRangeInches();

    if (sensor.isRangeValid() && distance < safeDistance) 
        return true;
    else
        return false;
}
};