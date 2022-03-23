package org.firstinspires.ftc.teamcode.subsystemtest;

import com.qualcomm.hardware.rev.RevTouchSensor;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.hardware.DcMotor;

public class Arm extends Subsystem {

    DcMotor armMotor;
    RevTouchSensor maxArmHeightLimit;

    double armMotorSpeed = 0.45;

    public Arm(OpMode opMode) {
        super(opMode);
    }


    @Override
    public void init() {

        hardwareMap = opMode.hardwareMap;

        armMotor = hardwareMap.get(DcMotor.class, "arm");
        maxArmHeightLimit = hardwareMap.get(RevTouchSensor.class, "MAHL");
        armMotor.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        armMotor.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
    }

    @Override
    public void run() {

        boolean maxArmHeightLimitReached = maxArmHeightLimit.isPressed();

        if (maxArmHeightLimitReached) {
            armMotor.setPower(0);
        }
        else {
            // Raises arm
            if (gamepad2.dpad_up) {
                armMotor.setPower(armMotorSpeed);
            }
            // Lowers arm
            else if (gamepad2.dpad_down) {
                armMotor.setPower(-armMotorSpeed);
            }
            else {

                if (gamepad1.dpad_up) {
                    armMotor.setPower(armMotorSpeed);
                }
                else if (gamepad1.dpad_down) {
                    armMotor.setPower(-armMotorSpeed);
                }
                else {
                    armMotor.setPower(0);
                }
            }
        }
    }
}
