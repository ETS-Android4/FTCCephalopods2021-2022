package org.firstinspires.ftc.teamcode.FTC_8088;

import com.acmerobotics.roadrunner.geometry.Pose2d;
import com.acmerobotics.roadrunner.geometry.Vector2d;

import com.qualcomm.hardware.rev.RevTouchSensor;
import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.Disabled;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.hardware.CRServo;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.util.ElapsedTime;

import org.firstinspires.ftc.robotcore.external.hardware.camera.WebcamName;
import org.firstinspires.ftc.teamcode.Barcode;
import org.firstinspires.ftc.teamcode.Scanner;
import org.firstinspires.ftc.teamcode.drive.DriveConstants;
import org.firstinspires.ftc.teamcode.drive.SampleMecanumDrive;
import org.firstinspires.ftc.teamcode.trajectorysequence.TrajectorySequence;
import org.openftc.easyopencv.OpenCvCamera;
import org.openftc.easyopencv.OpenCvCameraFactory;
import org.openftc.easyopencv.OpenCvCameraRotation;
import org.openftc.easyopencv.OpenCvWebcam;

@Autonomous(name = "Autonomous_RedLeft - Team_8088", group = "Autonomous - 8088")
public class Autonomous_8088_RedLeft extends LinearOpMode {

    // Motors
    DcMotor frontLeftMotor;
    DcMotor frontRightMotor;
    DcMotor backLeftMotor;
    DcMotor backRightMotor;

    DcMotor armMotor;
    DcMotor turret;
    DcMotor carouselWheelLeft;
    DcMotor carouselWheelRight;

    CRServo intake;
    CRServo capper;

    //   ColorSensor colorSensor;
    RevTouchSensor leftTurretLimit;
    RevTouchSensor rightTurretLimit;
    RevTouchSensor maxArmHeightLimit;


    // Motor Speed
    double frontLeftMotorSpeed;
    double frontRightMotorSpeed;
    double backLeftMotorSpeed;
    double backRightMotorSpeed;

    double intakeSpeed = 1;
    double turretSpeed = 0.5;
    double armMotorSpeed = 0.4;
    double carouselWheelSpeed = 0.6;

    double powerMultiplier = 1.0;

    private ElapsedTime runtime = new ElapsedTime();


    OpenCvWebcam webcam;

    @Override
    public void runOpMode() throws InterruptedException {

        frontLeftMotor   = hardwareMap.get(DcMotorEx.class, "FL");
        frontRightMotor  = hardwareMap.get(DcMotorEx.class, "FR");
        backLeftMotor    = hardwareMap.get(DcMotorEx.class, "BL");
        backRightMotor   = hardwareMap.get(DcMotorEx.class, "BR");

        turret           = hardwareMap.get(DcMotorEx.class, "TR");
        armMotor         = hardwareMap.get(DcMotorEx.class, "ARM");
        carouselWheelLeft    = hardwareMap.get(DcMotorEx.class, "CWL");
        carouselWheelRight = hardwareMap.get(DcMotorEx.class, "CWR");

        capper           = hardwareMap.get(CRServo.class, "CAP");
        intake           = hardwareMap.get(CRServo.class, "Intake");

        //      colorSensor      = hardwareMap.get(ColorSensor.class, "color");
        leftTurretLimit = hardwareMap.get(RevTouchSensor.class, "LTL");
        rightTurretLimit = hardwareMap.get(RevTouchSensor.class, "RTL");
        maxArmHeightLimit = hardwareMap.get(RevTouchSensor.class, "MAHL");



        // Set motor direction
        frontLeftMotor.setDirection(DcMotor.Direction.FORWARD);
        frontRightMotor.setDirection(DcMotor.Direction.REVERSE);
        backLeftMotor.setDirection(DcMotor.Direction.FORWARD);
        backRightMotor.setDirection(DcMotor.Direction.REVERSE);


        // Set ZERO POWER BEHAVIOR
        frontLeftMotor.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        frontRightMotor.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        backLeftMotor.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        backRightMotor.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);

        turret.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        armMotor.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        carouselWheelLeft.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        carouselWheelRight.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        capper.setPower(0);

        // Set up encoders
        armMotor.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        armMotor.setMode(DcMotor.RunMode.RUN_USING_ENCODER);

        turret.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        turret.setMode(DcMotor.RunMode.RUN_USING_ENCODER);


        // TODO: ROADRUNNER TRAJECTORIES

        SampleMecanumDrive drive = new SampleMecanumDrive(hardwareMap);

        Pose2d startPose = new Pose2d(-35, -64, Math.toRadians(0));
        drive.setPoseEstimate(startPose);

        TrajectorySequence storagePark1 = drive.trajectorySequenceBuilder(startPose)

                .UNSTABLE_addTemporalMarkerOffset(0, () -> moveArm(350))
                .lineToLinearHeading(new Pose2d(-66, -42, Math.toRadians(0)))
                .strafeRight(12,
                        SampleMecanumDrive.getVelocityConstraint(15, DriveConstants.MAX_ANG_VEL, DriveConstants.TRACK_WIDTH),
                        SampleMecanumDrive.getAccelerationConstraint(15)
                )
                .resetConstraints()

                .addTemporalMarker(3, () ->
                        carouselWheelRight.setPower(-0.6))
                .waitSeconds(3.5)
                .addTemporalMarker(6.5, () ->
                        carouselWheelRight.setPower(0))

                .UNSTABLE_addTemporalMarkerOffset(0, () -> moveArm(500))
                .UNSTABLE_addTemporalMarkerOffset(0.5, () -> moveTurret(-100))
                .lineToLinearHeading(new Pose2d(-53, -8, Math.toRadians(270)))
                .lineToLinearHeading(new Pose2d(-20, -10, Math.toRadians(270)))

                .UNSTABLE_addTemporalMarkerOffset(0, () -> intake.setPower(intakeSpeed))
                .waitSeconds(1.5)
                .UNSTABLE_addTemporalMarkerOffset(0.2, () -> intake.setPower(0))

                .UNSTABLE_addTemporalMarkerOffset(0.5, () -> moveTurret(0))
                .lineToLinearHeading(new Pose2d(-60, -24, Math.toRadians(0)))
                .UNSTABLE_addTemporalMarkerOffset(0, () -> moveArm(100))
                .strafeRight(7)

                .build();

        TrajectorySequence storagePark2 = drive.trajectorySequenceBuilder(startPose)

                .UNSTABLE_addTemporalMarkerOffset(0, () -> moveArm(350))
                .lineToLinearHeading(new Pose2d(-66, -40, Math.toRadians(0)))
                .strafeRight(13)

                .addTemporalMarker(3, () ->
                        carouselWheelRight.setPower(-0.6))
                .waitSeconds(3.5)
                .addTemporalMarker(6.5, () ->
                        carouselWheelRight.setPower(0))

                .UNSTABLE_addTemporalMarkerOffset(0, () -> moveArm(800))
                .UNSTABLE_addTemporalMarkerOffset(0.5, () -> moveTurret(-100))
                .lineToLinearHeading(new Pose2d(-53, -8, Math.toRadians(270)))
                .lineToLinearHeading(new Pose2d(-17, -10, Math.toRadians(270)))

                .UNSTABLE_addTemporalMarkerOffset(0, () -> intake.setPower(intakeSpeed))
                .waitSeconds(1.5)
                .UNSTABLE_addTemporalMarkerOffset(0, () -> intake.setPower(0))

                .UNSTABLE_addTemporalMarkerOffset(0.5, () -> moveTurret(0))
                .lineToLinearHeading(new Pose2d(-60, -24, Math.toRadians(0)))
                .UNSTABLE_addTemporalMarkerOffset(0, () -> moveArm(100))
                .strafeRight(7)

                .build();

        TrajectorySequence storagePark3 = drive.trajectorySequenceBuilder(startPose)

                .UNSTABLE_addTemporalMarkerOffset(0, () -> moveArm(350))
                .lineToLinearHeading(new Pose2d(-66, -40, Math.toRadians(0)))
                .strafeRight(13)

                .addTemporalMarker(3, () ->
                        carouselWheelRight.setPower(-0.6))
                .waitSeconds(3.5)
                .addTemporalMarker(6.5, () ->
                        carouselWheelRight.setPower(0))

                .UNSTABLE_addTemporalMarkerOffset(0, () -> moveArm(1250))
                .UNSTABLE_addTemporalMarkerOffset(0.5, () -> moveTurret(-100))
                .lineToLinearHeading(new Pose2d(-53, -8, Math.toRadians(270)))
                .lineToLinearHeading(new Pose2d(-12, -10, Math.toRadians(270)))

                .UNSTABLE_addTemporalMarkerOffset(0, () -> intake.setPower(intakeSpeed))
                .waitSeconds(1.5)
                .UNSTABLE_addTemporalMarkerOffset(0.3, () -> intake.setPower(0))

                .UNSTABLE_addTemporalMarkerOffset(0.5, () -> moveTurret(0))
                .lineToLinearHeading(new Pose2d(-60, -24, Math.toRadians(0)))
                .UNSTABLE_addTemporalMarkerOffset(0, () -> moveArm(100))
                .strafeRight(7)

                .build();

        TrajectorySequence warehousePark1 = drive.trajectorySequenceBuilder(startPose)


                .UNSTABLE_addTemporalMarkerOffset(0, () -> moveArm(350))
                .lineToLinearHeading(new Pose2d(-66, -40, Math.toRadians(0)))
                .strafeRight(13)

                .addTemporalMarker(3, () ->
                        carouselWheelRight.setPower(-0.6))
                .waitSeconds(3.5)
                .addTemporalMarker(6.5, () ->
                        carouselWheelRight.setPower(0))

                .UNSTABLE_addTemporalMarkerOffset(0, () -> moveArm(500))
                .UNSTABLE_addTemporalMarkerOffset(0.5, () -> moveTurret(-100))
                .lineToLinearHeading(new Pose2d(-53, -8, Math.toRadians(270)))
                .lineToLinearHeading(new Pose2d(-20, -10, Math.toRadians(270)))

                .UNSTABLE_addTemporalMarkerOffset(0, () -> intake.setPower(intakeSpeed))
                .waitSeconds(1.5)
                .UNSTABLE_addTemporalMarkerOffset(0.3, () -> intake.setPower(0))

                .UNSTABLE_addTemporalMarkerOffset(0.5, () -> moveTurret(0))
                .lineToLinearHeading(new Pose2d(-55, -10, Math.toRadians(0)))
                .UNSTABLE_addTemporalMarkerOffset(0, () -> moveArm(300))

                .strafeRight(20)
                .lineToLinearHeading(new Pose2d(-25, -70, Math.toRadians(180)))
                .waitSeconds(8)
                .lineTo(new Vector2d(42, -66))

                .build();

        TrajectorySequence warehousePark2 = drive.trajectorySequenceBuilder(startPose)


                .UNSTABLE_addTemporalMarkerOffset(0, () -> moveArm(350))
                .lineToLinearHeading(new Pose2d(-66, -40, Math.toRadians(0)))
                .strafeRight(13)

                .addTemporalMarker(3, () ->
                        carouselWheelRight.setPower(-0.6))
                .waitSeconds(3.5)
                .addTemporalMarker(6.5, () ->
                        carouselWheelRight.setPower(0))

                .UNSTABLE_addTemporalMarkerOffset(0, () -> moveArm(800))
                .UNSTABLE_addTemporalMarkerOffset(0.5, () -> moveTurret(-100))
                .lineToLinearHeading(new Pose2d(-53, -8, Math.toRadians(270)))
                .lineToLinearHeading(new Pose2d(-18, -10, Math.toRadians(270)))

                .UNSTABLE_addTemporalMarkerOffset(0, () -> intake.setPower(intakeSpeed))
                .waitSeconds(1.5)
                .UNSTABLE_addTemporalMarkerOffset(0, () -> intake.setPower(0))

                .UNSTABLE_addTemporalMarkerOffset(0.5, () -> moveTurret(0))
                .lineToLinearHeading(new Pose2d(-55, -10, Math.toRadians(0)))
                .UNSTABLE_addTemporalMarkerOffset(0, () -> moveArm(300))

                .strafeRight(20)
                .lineToLinearHeading(new Pose2d(-25, -70, Math.toRadians(180)))
                .waitSeconds(8)
                .lineTo(new Vector2d(42, -66))

                .build();

        TrajectorySequence warehousePark3 = drive.trajectorySequenceBuilder(startPose)



                .UNSTABLE_addTemporalMarkerOffset(0, () -> moveArm(350))
                .lineToLinearHeading(new Pose2d(-66, -40, Math.toRadians(0)))
                .strafeRight(13)

                .addTemporalMarker(3, () ->
                        carouselWheelRight.setPower(-0.6))
                .waitSeconds(3.5)
                .addTemporalMarker(6.5, () ->
                        carouselWheelRight.setPower(0))

                .UNSTABLE_addTemporalMarkerOffset(0, () -> moveArm(1250))
                .UNSTABLE_addTemporalMarkerOffset(0.5, () -> moveTurret(-100))
                .lineToLinearHeading(new Pose2d(-53, -8, Math.toRadians(270)))
                .lineToLinearHeading(new Pose2d(-12, -10, Math.toRadians(270)))

                .UNSTABLE_addTemporalMarkerOffset(0, () -> intake.setPower(intakeSpeed))
                .waitSeconds(1.5)
                .UNSTABLE_addTemporalMarkerOffset(0, () -> intake.setPower(0))

                .UNSTABLE_addTemporalMarkerOffset(0.5, () -> moveTurret(0))
                .lineToLinearHeading(new Pose2d(-55, -10, Math.toRadians(0)))
                .UNSTABLE_addTemporalMarkerOffset(0, () -> moveArm(300))

                .strafeRight(20)
                .lineToLinearHeading(new Pose2d(-25, -70, Math.toRadians(180)))
                .waitSeconds(8)
                .lineTo(new Vector2d(42, -66))

                .build();


        telemetry.addData("Status", "Initialized");
        telemetry.addData("Status", "Run Time: " + runtime.toString());
        telemetry.update();


        int cameraMonitorViewId = hardwareMap.appContext.getResources().getIdentifier("cameraMonitorViewId", "id", hardwareMap.appContext.getPackageName());
        webcam = OpenCvCameraFactory.getInstance().createWebcam(hardwareMap.get(WebcamName.class, "Webcam 1"), cameraMonitorViewId);
        Scanner scanner = new Scanner(telemetry);
        webcam.setPipeline(scanner);

        webcam.setMillisecondsPermissionTimeout(2500); // Timeout for obtaining permission is configurable. Set before opening.
        webcam.openCameraDeviceAsync(new OpenCvCamera.AsyncCameraOpenListener()
        {
            @Override
            public void onOpened()
            {
                webcam.startStreaming(320, 240, OpenCvCameraRotation.UPRIGHT);

            }

            @Override
            public void onError(int errorCode)
            {
            }
        });


        waitForStart();// TODO: PLACE AUTONOMOUS CODE AFTER THIS LINE
        runtime.reset();

        if (isStopRequested()) return;

        Barcode result = scanner.getResult();


        switch (result) {

            case LEFT:

                drive.followTrajectorySequence(storagePark1);

               // drive.followTrajectorySequence(warehousePark1);

                break;

            case MIDDLE:

                drive.followTrajectorySequence(storagePark2);

              //    drive.followTrajectorySequence(warehousePark2);

                break;

            case RIGHT:

                drive.followTrajectorySequence(storagePark3);

              //  drive.followTrajectorySequence(warehousePark3);

                break;
        }
    }


    // ROBOT AUTONOMOUS FUNCTIONS

    public void moveArm(int targetPosition) {
        armMotor.setTargetPosition(targetPosition);
        armMotor.setMode(DcMotor.RunMode.RUN_TO_POSITION);
        armMotor.setPower(armMotorSpeed);
    }

    public void moveTurret(int targetPosition) {
        turret.setTargetPosition(targetPosition);
        turret.setMode(DcMotor.RunMode.RUN_TO_POSITION);
        turret.setPower(turretSpeed);
    }



}
