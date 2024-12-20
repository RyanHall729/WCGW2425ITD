/* Copyright (c) 2021 FIRST. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted (subject to the limitations in the disclaimer below) provided that
 * the following conditions are met:
 *
 * Redistributions of source code must retain the above copyright notice, this list
 * of conditions and the following disclaimer.
 *
 * Redistributions in binary form must reproduce the above copyright notice, this
 * list of conditions and the following disclaimer in the documentation and/or
 * other materials provided with the distribution.
 *
 * Neither the name of FIRST nor the names of its contributors may be used to endorse or
 * promote products derived from this software without specific prior written permission.
 *
 * NO EXPRESS OR IMPLIED LICENSES TO ANY PARTY'S PATENT RIGHTS ARE GRANTED BY THIS
 * LICENSE. THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
 * THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package org.firstinspires.ftc.teamcode;

import com.qualcomm.hardware.rev.RevHubOrientationOnRobot;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.hardware.CRServo;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.hardware.IMU;
import com.qualcomm.robotcore.util.ElapsedTime;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.Servo;

/*
 * This file contains an example of a Linear "OpMode".
 * An OpMode is a 'program' that runs in either the autonomous or the teleop period of an FTC match.
 * The names of OpModes appear on the menu of the FTC Driver Station.
 * When a selection is made from the menu, the corresponding OpMode is executed.
 *
 * This particular OpMode illustrates driving a 4-motor Omni-Directional (or Holonomic) robot.
 * This code will work with either a Mecanum-Drive or an X-Drive train.
 * Both of these drives are illustrated at https://gm0.org/en/latest/docs/robot-design/drivetrains/holonomic.html
 * Note that a Mecanum drive must display an X roller-pattern when viewed from above.
 *
 * Also note that it is critical to set the correct rotation direction for each motor.  See details below.
 *
 * Holonomic drives provide the ability for the robot to move in three axes (directions) simultaneously.
 * Each motion axis is controlled by one Joystick axis.
 *
 * 1) Axial:    Driving forward and backward               Left-joystick Forward/Backward
 * 2) Lateral:  Strafing right and left                     Left-joystick Right and Left
 * 3) Yaw:      Rotating Clockwise and counter clockwise    Right-joystick Right and Left
 *
 * This code is written assuming that the right-side motors need to be reversed for the robot to drive forward.
 * When you first test your robot, if it moves backward when you push the left stick forward, then you must flip
 * the direction of all 4 motors (see code below).
 *
 * Use Android Studio to Copy this Class, and Paste it into your team's code folder with a new name.
 * Remove or comment out the @Disabled line to add this OpMode to the Driver Station OpMode list
 */

@com.qualcomm.robotcore.eventloop.opmode.TeleOp(name="Basic: Sink", group="Linear OpMode")
//@Disabled
public class TeleOp extends LinearOpMode {

    // Declare OpMode members for each of the 4 motors.
//    private ElapsedTime runtime = new ElapsedTime();
//    private ElapsedTime intakeStopwatch = new ElapsedTime();
//    private ElapsedTime elbowStopwatch = new ElapsedTime();
//    public DcMotor leftFront = null;
//    public DcMotor leftBack = null;
//    public DcMotor rightFront = null;
//    public DcMotor rightBack = null;
//    public Servo extender = null;
    public DcMotor leftFront = null;
    public DcMotor leftBack = null;
    public DcMotor rightFront = null;
    public DcMotor rightBack = null;
    public CRServo intake = null;
    public Servo extender = null;
    public DcMotor elbowTop = null;
    public DcMotor elbowBottom = null;
    public ElapsedTime intakeStopwatch = null;
    public boolean isOutaking = false;
    public boolean elbowFunctionUp = false;
    public boolean elbowFunctionDown = false;
    public IMU imu = null;
    public int armPosition = 0;
    public Pcontroller pcontrollerArm = new Pcontroller(.005);
    public int elbowMaxTicks = 1700;
    public int state = 0;

    enum GrabAndDrop
    {
        GRAB_SAMPLE,
        DRIVE,
        DROP,
        HOME,
    }

    GrabAndDrop grabAndDrop = GrabAndDrop.GRAB_SAMPLE;

    @Override
    public void runOpMode() {

        // Initialize the hardware variables. Note that the strings used here must correspond
        // to the names assigned during the robot configuration step on the DS or RC devices.
        leftFront = hardwareMap.get(DcMotor.class, "leftFront");
        leftBack = hardwareMap.get(DcMotor.class, "leftBack");
        rightFront = hardwareMap.get(DcMotor.class, "rightFront");
        rightBack = hardwareMap.get(DcMotor.class, "rightBack");

        intakeStopwatch = new ElapsedTime();
//        intake = hardwareMap.get(CRServo.class, "intake");
        //extender.setPosition(0);


        elbowTop = hardwareMap.get(DcMotor.class, "elbowTop");
        elbowBottom = hardwareMap.get(DcMotor.class, "elbowBottom");
        intake = hardwareMap.get(CRServo.class, "intake"); //port 0
        extender = hardwareMap.get(Servo.class, "extender"); //port 1
        elbowTop.setDirection(DcMotorSimple.Direction.REVERSE);
        elbowBottom.setDirection(DcMotorSimple.Direction.REVERSE);
        elbowTop.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        elbowBottom.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);

        imu = hardwareMap.get(IMU.class, "imu");
        RevHubOrientationOnRobot hubOrientationOnRobot = new RevHubOrientationOnRobot(RevHubOrientationOnRobot.LogoFacingDirection.LEFT, RevHubOrientationOnRobot.UsbFacingDirection.UP);
        IMU.Parameters parameters = new IMU.Parameters(hubOrientationOnRobot);
        imu.initialize(parameters);

//        extender = hardwareMap.get(Servo.class, "extender");

        // ########################################################################################
        // !!!            IMPORTANT Drive Information. Test your motor directions.            !!!!!
        // ########################################################################################
        // Most robots need the motors on one side to be reversed to drive forward.
        // The motor reversals shown here are for a "direct drive" robot (the wheels turn the same direction as the motor shaft)
        // If your robot has additional gear reductions or uses a right-angled drive, it's important to ensure
        // that your motors are turning in the correct direction.  So, start out with the reversals here, BUT
        // when you first test your robot, push the left joystick forward and observe the direction the wheels turn.
        // Reverse the direction (flip FORWARD <-> REVERSE ) of any wheel that runs backward
        // Keep testing until ALL the wheels move the robot forward when you push the left joystick forward.
        leftFront.setDirection(DcMotor.Direction.REVERSE);
        leftBack.setDirection(DcMotor.Direction.REVERSE);
        rightFront.setDirection(DcMotor.Direction.FORWARD);
        rightBack.setDirection(DcMotor.Direction.FORWARD);

        // Wait for the game to start (driver presses START)
        telemetry.addData("Status", "Initialized");

        //intake = hardwareMap.get(CRServo.class, "intake");
        pcontrollerArm.setInputRange(0, elbowMaxTicks);
        pcontrollerArm.setSetPoint(1450);
        pcontrollerArm.setOutputRange(.01,.99);
        pcontrollerArm.setThresholdValue(0);
        telemetry.update();



        waitForStart();
//        runtime.reset();

        // run until the end of the match (driver presses STOP)
        double leftBackPower = 0;
        double leftFrontPower = 0;
        double rightBackPower = 0;
        double rightFrontPower = 0;
        while (opModeIsActive()) {


            double max;

            // POV Mode uses left joystick to go forward & strafe, and right joystick to rotate.
            double axial = -gamepad1.left_stick_y;  // Note: pushing stick forward gives negative value
            double lateral = gamepad1.left_stick_x;
            double yaw = gamepad1.right_stick_x;

            // Combine the joystick requests for each axis-motion to determine each wheel's power.
            // Set up a variable for each drive wheel to save the power level for telemetry.
            leftFrontPower = axial + lateral + yaw;
            rightFrontPower = axial - lateral - yaw;
            leftBackPower = axial - lateral + yaw;
            rightBackPower = axial + lateral - yaw;

            // Normalize the values so no wheel power exceeds 100%
            // This ensures that the robot maintains the desired motion.
            max = Math.max(Math.abs(leftFrontPower), Math.abs(rightFrontPower));
            max = Math.max(max, Math.abs(leftBackPower));
            max = Math.max(max, Math.abs(rightBackPower));

            if (max > 1.0) {
                leftFrontPower /= max;
                rightFrontPower /= max;
                leftBackPower /= max;
                rightBackPower /= max;
            }

            // This is test code:
            //
            // Uncomment the following code to test your motor directions.
            // Each button should make the corresponding motor run FORWARD.
            //   1) First get all the motors to take to correct positions on the robot
            //      by adjusting your Robot Configuration if necessary.
            //   2) Then make sure they run in the correct direction by modifying the
            //      the setDirection() calls above.
            // Once the correct motors move in the correct direction re-comment this code.

            /*
            leftFrontPower  = gamepad1.x ? 1.0 : 0.0;  // X gamepad
            leftBackPower   = gamepad1.a ? 1.0 : 0.0;  // A gamepad
            rightFrontPower = gamepad1.y ? 1.0 : 0.0;  // Y gamepad
            rightBackPower  = gamepad1.b ? 1.0 : 0.0;  // B gamepad
            */

            // Send calculated power to wheels
            leftFront.setPower(leftFrontPower);
            rightFront.setPower(rightFrontPower);
            leftBack.setPower(leftBackPower);
            rightBack.setPower(rightBackPower);

            //extender
            if (gamepad1.left_bumper)
            {
                extender.setPosition(1);
            }
            else if (gamepad1.y)
            {
                extender.setPosition(0);
            }
//


            //intake
            if (gamepad1.x) {
                intake.setPower(-1);
                intakeStopwatch.reset();
            } else if (intakeStopwatch.seconds() >= 2.05) {
                intake.setPower(0);
            }

            //outake
            if (gamepad1.b) {
                intake.setPower(1);
                intakeStopwatch.reset();
                isOutaking = true;
            } else if (intakeStopwatch.seconds() >= 15 && isOutaking) {
                intake.setPower(0);
                isOutaking = false;
            }
            //elbowup
            if (gamepad1.dpad_up)
            {
                elbowTop.setPower(.45);
                elbowBottom.setPower(.45);
                pcontrollerArm.setSetPoint(elbowTop.getCurrentPosition());
            }

            //elbowdown
           else if (gamepad1.dpad_down)
            {
                elbowTop.setPower(-.45);
                elbowBottom.setPower(-.45);
                pcontrollerArm.setSetPoint(elbowTop.getCurrentPosition());
            }
           else
            {
                updateArmPosition();
            }

           //Untested state machine
           switch (grabAndDrop) {
               case GRAB_SAMPLE:
               {
                   if(gamepad1.a && state == 0)
                   {
                       pcontrollerArm.setSetPoint(Constants.LIFT_PICKUP);
                       intake.setPower(1);
                       extender.setPosition(Constants.EXTENDER_OUT);
                       intakeStopwatch.reset();
                       state++;
                       grabAndDrop = GrabAndDrop.DRIVE;
                   }
                   break;
               }
               case DRIVE:
               {
                   //if the intake has been running for a bit then turn it off so we can drive
                   if(state == 1 && gamepad1.right_bumper)
                   {
                       intake.setPower(0);
                       extender.setPosition(Constants.EXTENDER_SAFE);
                       pcontrollerArm.setSetPoint(500);
                       state++;
                       intakeStopwatch.reset();
                       grabAndDrop = GrabAndDrop.DROP;
                   }
                   break;
               }
               case DROP:
               {
                   if(gamepad1.right_bumper && state == 2 && intakeStopwatch.seconds() > 1)
                   {
                       pcontrollerArm.setSetPoint(Constants.LIFT_UP);
                       extender.setPosition(Constants.EXTENDER_OUT);
                       intake.setPower(-1);
                       intakeStopwatch.reset();
                       state++;
                       grabAndDrop = GrabAndDrop.HOME;
                   }
                   break;
               }               case HOME:
               {
                   if (gamepad1.right_trigger > 0.2 && state == 3)
                   {
                       pcontrollerArm.setSetPoint(Constants.LIFT_HOME);
                       extender.setPosition(Constants.EXTENDER_SAFE);
                       intake.setPower(0);
                       intakeStopwatch.reset();
                       state = 0;
                       grabAndDrop = GrabAndDrop.GRAB_SAMPLE;
                   }
                   break;
               }

           }

           if(gamepad1.right_trigger > 0.2)
           {
               grabAndDrop = GrabAndDrop.HOME;
           }
            //get rotation
            //turn the robot
            // apply power to specific wheels


            // Show the elapsed game time and wheel power.
//            telemetry.addData("Status", "Run Time: " + runtime.toString());
            telemetry.addData("Front left/Right", "%4.2f, %4.2f", leftFrontPower, rightFrontPower);
            telemetry.addData("Back  left/Right", "%4.2f, %4.2f", leftBackPower, rightBackPower);
            telemetry.addData("Is outake Active:", isOutaking);
            telemetry.addData("intake:", intake.getPower());
            telemetry.addData("rotation", imu.getRobotYawPitchRollAngles());
            telemetry.addData("elbowTop Power", elbowTop.getPower());
            telemetry.addData("elbowBottom Power", elbowBottom.getPower());
            telemetry.addData("elbowTop Position", elbowTop.getCurrentPosition());
            telemetry.addData("elbowBottom Position", elbowBottom.getCurrentPosition());
            telemetry.addData("pcontroller set pt ", pcontrollerArm.setPoint);
            telemetry.addData("state", state);


            //telemetry.addData("intake", intake.getPower());
            telemetry.update();
        }
    }

    public void updateArmPosition ()
    {
        armPosition = elbowTop.getCurrentPosition();
        if (armPosition < pcontrollerArm.setPoint)
        {
            elbowTop.setPower(.01 + pcontrollerArm.getComputedOutput(armPosition));
            elbowBottom.setPower(.01 + pcontrollerArm.getComputedOutput(armPosition));
        }
        else
        {
            elbowTop.setPower(.01 - pcontrollerArm.getComputedOutput(armPosition));
            elbowBottom.setPower(.01 - pcontrollerArm.getComputedOutput(armPosition));
        }
    }
}

