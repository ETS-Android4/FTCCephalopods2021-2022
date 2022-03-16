package org.firstinspires.ftc.teamcode.FTC_8088;

import com.acmerobotics.roadrunner.geometry.Pose2d;
import com.acmerobotics.roadrunner.geometry.Vector2d;
import com.qualcomm.hardware.bosch.BNO055IMU;
import com.qualcomm.hardware.rev.RevColorSensorV3;
import com.qualcomm.hardware.rev.RevTouchSensor;
import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.hardware.CRServo;
import com.qualcomm.robotcore.hardware.ColorSensor;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.util.ElapsedTime;

import org.firstinspires.ftc.robotcore.external.hardware.camera.WebcamName;
import org.firstinspires.ftc.robotcore.external.navigation.AngleUnit;
import org.firstinspires.ftc.robotcore.external.navigation.AxesOrder;
import org.firstinspires.ftc.robotcore.external.navigation.AxesReference;
import org.firstinspires.ftc.robotcore.external.navigation.DistanceUnit;
import org.firstinspires.ftc.robotcore.external.navigation.Orientation;
import org.firstinspires.ftc.teamcode.Barcode;
import org.firstinspires.ftc.teamcode.Hardware;
import org.firstinspires.ftc.teamcode.PIDController;
import org.firstinspires.ftc.teamcode.Scanner;
import org.firstinspires.ftc.teamcode.drive.DriveConstants;
import org.firstinspires.ftc.teamcode.drive.SampleMecanumDrive;
import org.firstinspires.ftc.teamcode.trajectorysequence.TrajectorySequence;
import org.openftc.easyopencv.OpenCvCamera;
import org.openftc.easyopencv.OpenCvCameraFactory;
import org.openftc.easyopencv.OpenCvCameraRotation;
import org.openftc.easyopencv.OpenCvWebcam;

@Autonomous(name = "Autonomous_RedRight - Team_8088", group = "Autonomous - 8088")
public class Autonomous_8088_RedRight extends LinearOpMode {

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

    RevTouchSensor leftTurretLimit;
    RevTouchSensor rightTurretLimit;
    RevTouchSensor maxArmHeightLimit;
    RevColorSensorV3 colorSensor;
    ColorSensor colorSensor2;


    // Motor Speed
    double frontLeftMotorSpeed;
    double frontRightMotorSpeed;
    double backLeftMotorSpeed;
    double backRightMotorSpeed;

    double intakeSpeed = 1;
    double turretSpeed = 0.35;
    double armMotorSpeed = 0.4;
    double carouselWheelSpeed = 0.6;

    double powerMultiplier = 1.0;


    ElapsedTime runtime = new ElapsedTime();
    BNO055IMU imu;

    OpenCvWebcam webcam;


    @Override
    public void runOpMode() throws InterruptedException {

        frontLeftMotor   = hardwareMap.get(DcMotor.class, "FL");
        frontRightMotor  = hardwareMap.get(DcMotor.class, "FR");
        backLeftMotor    = hardwareMap.get(DcMotor.class, "BL");
        backRightMotor   = hardwareMap.get(DcMotor.class, "BR");

        turret           = hardwareMap.get(DcMotor.class, "TR");
        armMotor         = hardwareMap.get(DcMotor.class, "ARM");
        carouselWheelLeft    = hardwareMap.get(DcMotor.class, "CWL");
        carouselWheelRight = hardwareMap.get(DcMotor.class, "CWR");

        capper           = hardwareMap.get(CRServo.class, "CAP");
        intake           = hardwareMap.get(CRServo.class, "Intake");

        colorSensor      = hardwareMap.get(RevColorSensorV3.class, "CS");
        colorSensor2     = hardwareMap.get(ColorSensor.class, "CS2");
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



        SampleMecanumDrive drive = new SampleMecanumDrive(hardwareMap);

        Pose2d startPose = new Pose2d(12,-64, Math.toRadians(0));
        Pose2d depositPose = new Pose2d(-3,-24, Math.toRadians(55));
        Pose2d alignmentPose = new Pose2d(8, -64, Math.toRadians(0));
        Vector2d startVector = new Vector2d(12, -64);


        drive.setPoseEstimate(startPose);

        TrajectorySequence cycle1SharedPark = drive.trajectorySequenceBuilder(startPose)

                .UNSTABLE_addTemporalMarkerOffset(0, () -> moveArm(800))
                .UNSTABLE_addTemporalMarkerOffset(1, () -> moveTurret(-100))
                .UNSTABLE_addTemporalMarkerOffset(2, () -> moveArm(350))
                .lineToLinearHeading(new Pose2d(-12,-37, Math.toRadians(0)))
                .UNSTABLE_addTemporalMarkerOffset(0.5, () -> intake.setPower(intakeSpeed))
                .waitSeconds(1.5)
                .UNSTABLE_addTemporalMarkerOffset(0, () -> intake.setPower(0))
                .UNSTABLE_addTemporalMarkerOffset(0, () -> moveArm(800))
                .UNSTABLE_addTemporalMarkerOffset(0.3, () -> moveTurretPower(-10, 0.5))
                .lineToLinearHeading(new Pose2d(8, -66, Math.toRadians(0)))
                .UNSTABLE_addTemporalMarkerOffset(0, () -> moveArm(350))
                .UNSTABLE_addTemporalMarkerOffset(0.45, () -> moveArm(0))

                .forward(32)
                .UNSTABLE_addTemporalMarkerOffset(0, () -> intake.setPower(-intakeSpeed))
                .UNSTABLE_addTemporalMarkerOffset(1.5, () -> intake.setPower(0))
                .splineTo(new Vector2d(52, -66), Math.toRadians(0),
                        SampleMecanumDrive.getVelocityConstraint(15, DriveConstants.MAX_ANG_VEL, DriveConstants.TRACK_WIDTH),
                        SampleMecanumDrive.getAccelerationConstraint(15)
                )
                .resetConstraints()

                .UNSTABLE_addTemporalMarkerOffset(0.5, () -> moveArm(500))
                .lineTo(new Vector2d(11,-67))
                .UNSTABLE_addTemporalMarkerOffset(0, () -> moveArm(1250))
                .UNSTABLE_addTemporalMarkerOffset(0.5, () -> moveTurret(-100))
                .lineToLinearHeading(new Pose2d(-2,-28, Math.toRadians(55)))
                .UNSTABLE_addTemporalMarkerOffset(0, () -> intake.setPower(intakeSpeed))
                .waitSeconds(1)
                .UNSTABLE_addTemporalMarkerOffset(0, () -> intake.setPower(0))
                .UNSTABLE_addTemporalMarkerOffset(0, () -> moveTurret(-15))
                .UNSTABLE_addTemporalMarkerOffset(1.8, () -> moveArm(350))
                .lineToLinearHeading(new Pose2d(12, -68, Math.toRadians(0)))
                .UNSTABLE_addTemporalMarkerOffset(0.35, () -> moveArm(0))

                .forward(35)
                .UNSTABLE_addTemporalMarkerOffset(0, () -> intake.setPower(-intakeSpeed))
                .UNSTABLE_addTemporalMarkerOffset(1.5, () -> intake.setPower(0))
                .splineTo(new Vector2d(58, -68), Math.toRadians(0),
                        SampleMecanumDrive.getVelocityConstraint(15, DriveConstants.MAX_ANG_VEL, DriveConstants.TRACK_WIDTH),
                        SampleMecanumDrive.getAccelerationConstraint(15)
                )
                .resetConstraints()

                .UNSTABLE_addTemporalMarkerOffset(0.5, () -> moveArm(500))
                .lineTo(new Vector2d(12,-68))
                .UNSTABLE_addTemporalMarkerOffset(0, () -> moveArm(1250))
                .UNSTABLE_addTemporalMarkerOffset(0.5, () -> moveTurret(-100))
                .lineToLinearHeading(new Pose2d(0,-28, Math.toRadians(55)))
                .UNSTABLE_addTemporalMarkerOffset(0, () -> intake.setPower(intakeSpeed))
                .waitSeconds(1)
                .UNSTABLE_addTemporalMarkerOffset(0, () -> intake.setPower(0))
                .UNSTABLE_addTemporalMarkerOffset(0, () -> moveTurret(-20))
                .UNSTABLE_addTemporalMarkerOffset(1.8, () -> moveArm(350))
                .lineToLinearHeading(new Pose2d(12, -69, Math.toRadians(0)))


                .forward(30)
                .strafeLeft(25)
                .splineToLinearHeading(new Pose2d(68, -30, Math.toRadians(270)), Math.toRadians(0))

                .build();

        TrajectorySequence cycle2SharedPark = drive.trajectorySequenceBuilder(startPose)


                .UNSTABLE_addTemporalMarkerOffset(0, () -> moveArm(900))
                .UNSTABLE_addTemporalMarkerOffset(0.5, () -> moveTurret(-85))
                .lineToLinearHeading(new Pose2d(-2,-30, Math.toRadians(55)))
                .UNSTABLE_addTemporalMarkerOffset(0, () -> intake.setPower(intakeSpeed))
                .waitSeconds(1)
                .UNSTABLE_addTemporalMarkerOffset(0, () -> intake.setPower(0))
                .UNSTABLE_addTemporalMarkerOffset(0, () -> moveTurret(0))
                .UNSTABLE_addTemporalMarkerOffset(1.8, () -> moveArm(350))
                .lineToLinearHeading(alignmentPose)
                .UNSTABLE_addTemporalMarkerOffset(0.35, () -> moveArm(0))

                .forward(32)
                .UNSTABLE_addTemporalMarkerOffset(0, () -> intake.setPower(-intakeSpeed))
                .UNSTABLE_addTemporalMarkerOffset(1.5, () -> intake.setPower(0))
                .splineTo(new Vector2d(50, -64), Math.toRadians(0),
                        SampleMecanumDrive.getVelocityConstraint(15, DriveConstants.MAX_ANG_VEL, DriveConstants.TRACK_WIDTH),
                        SampleMecanumDrive.getAccelerationConstraint(15)
                )
                .resetConstraints()

                .UNSTABLE_addTemporalMarkerOffset(0.5, () -> moveArm(500))
                .lineToLinearHeading(new Pose2d(11,-66, Math.toRadians(0)))
                .UNSTABLE_addTemporalMarkerOffset(0, () -> moveArm(1250))
                .UNSTABLE_addTemporalMarkerOffset(0.5, () -> moveTurret(-100))
                .lineToLinearHeading(depositPose)
                .UNSTABLE_addTemporalMarkerOffset(0, () -> intake.setPower(intakeSpeed))
                .waitSeconds(1)
                .UNSTABLE_addTemporalMarkerOffset(0, () -> intake.setPower(0))
                .UNSTABLE_addTemporalMarkerOffset(0, () -> moveTurret(-20))
                .UNSTABLE_addTemporalMarkerOffset(1.8, () -> moveArm(350))
                .lineToLinearHeading(new Pose2d(12, -66, Math.toRadians(0)))
                .UNSTABLE_addTemporalMarkerOffset(0.35, () -> moveArm(0))


                .forward(35)
                .UNSTABLE_addTemporalMarkerOffset(0, () -> intake.setPower(-intakeSpeed))
                .UNSTABLE_addTemporalMarkerOffset(1.5, () -> intake.setPower(0))
                .splineTo(new Vector2d(54, -67), Math.toRadians(0),
                        SampleMecanumDrive.getVelocityConstraint(15, DriveConstants.MAX_ANG_VEL, DriveConstants.TRACK_WIDTH),
                        SampleMecanumDrive.getAccelerationConstraint(15)
                )
                .resetConstraints()

                .UNSTABLE_addTemporalMarkerOffset(0, () -> moveArm(500))
                .lineToLinearHeading(new Pose2d(12, -67, Math.toRadians(0)))
                .UNSTABLE_addTemporalMarkerOffset(0, () -> moveArm(1250))
                .UNSTABLE_addTemporalMarkerOffset(0.5, () -> moveTurret(-100))
                .lineToLinearHeading(depositPose)
                .UNSTABLE_addTemporalMarkerOffset(0, () -> intake.setPower(intakeSpeed))
                .waitSeconds(1)
                .UNSTABLE_addTemporalMarkerOffset(0, () -> intake.setPower(0))
                .UNSTABLE_addTemporalMarkerOffset(0, () -> moveTurret(-5))
                .UNSTABLE_addTemporalMarkerOffset(1.8, () -> moveArm(350))
                .lineToLinearHeading(new Pose2d(12, -67, Math.toRadians(0)))


                .forward(30)
                .strafeLeft(25)
                .splineToLinearHeading(new Pose2d(68, -30, Math.toRadians(270)), Math.toRadians(0))

                .build();

        TrajectorySequence cycle3SharedPark = drive.trajectorySequenceBuilder(startPose)


                .UNSTABLE_addTemporalMarkerOffset(0, () -> moveArm(1250))
                .UNSTABLE_addTemporalMarkerOffset(0.5, () -> moveTurret(-100))
                .lineToLinearHeading(new Pose2d(0,-20, Math.toRadians(65)))
                .UNSTABLE_addTemporalMarkerOffset(0, () -> intake.setPower(intakeSpeed))
                .waitSeconds(1)
                .UNSTABLE_addTemporalMarkerOffset(0, () -> intake.setPower(0))
                .UNSTABLE_addTemporalMarkerOffset(0, () -> moveTurret(0))
                .UNSTABLE_addTemporalMarkerOffset(1.8, () -> moveArm(350))
                .lineToLinearHeading(alignmentPose)
                .UNSTABLE_addTemporalMarkerOffset(0.35, () -> moveArm(0))

                .forward(32)
                .UNSTABLE_addTemporalMarkerOffset(0, () -> intake.setPower(-intakeSpeed))
                .UNSTABLE_addTemporalMarkerOffset(1.5, () -> intake.setPower(0))
                .splineTo(new Vector2d(50, -64), Math.toRadians(0),
                        SampleMecanumDrive.getVelocityConstraint(15, DriveConstants.MAX_ANG_VEL, DriveConstants.TRACK_WIDTH),
                        SampleMecanumDrive.getAccelerationConstraint(15)
                )
                .resetConstraints()


                .UNSTABLE_addTemporalMarkerOffset(0.5, () -> moveArm(500))
                .lineToLinearHeading(new Pose2d(11,-66, Math.toRadians(0)))
                .UNSTABLE_addTemporalMarkerOffset(0, () -> moveArm(1250))
                .UNSTABLE_addTemporalMarkerOffset(0.5, () -> moveTurret(-100))
                .lineToLinearHeading(depositPose)
                .UNSTABLE_addTemporalMarkerOffset(0, () -> intake.setPower(intakeSpeed))
                .waitSeconds(1)
                .UNSTABLE_addTemporalMarkerOffset(0, () -> intake.setPower(0))
                .UNSTABLE_addTemporalMarkerOffset(0, () -> moveTurret(-20))
                .UNSTABLE_addTemporalMarkerOffset(1.8, () -> moveArm(350))
                .lineToLinearHeading(new Pose2d(12, -66, Math.toRadians(0)))
                .UNSTABLE_addTemporalMarkerOffset(0.35, () -> moveArm(0))


                .forward(35)
                .UNSTABLE_addTemporalMarkerOffset(0, () -> intake.setPower(-intakeSpeed))
                .UNSTABLE_addTemporalMarkerOffset(1.5, () -> intake.setPower(0))
                .splineTo(new Vector2d(54, -66), Math.toRadians(0),
                        SampleMecanumDrive.getVelocityConstraint(15, DriveConstants.MAX_ANG_VEL, DriveConstants.TRACK_WIDTH),
                        SampleMecanumDrive.getAccelerationConstraint(15)
                )
                .resetConstraints()

                .UNSTABLE_addTemporalMarkerOffset(0, () -> moveArm(500))
                .lineToLinearHeading(new Pose2d(12,-67, Math.toRadians(0)))
                .UNSTABLE_addTemporalMarkerOffset(0, () -> moveArm(1250))
                .UNSTABLE_addTemporalMarkerOffset(0.5, () -> moveTurret(-100))
                .lineToLinearHeading(depositPose)
                .UNSTABLE_addTemporalMarkerOffset(0, () -> intake.setPower(intakeSpeed))
                .waitSeconds(1)
                .UNSTABLE_addTemporalMarkerOffset(0, () -> intake.setPower(0))
                .UNSTABLE_addTemporalMarkerOffset(0, () -> moveTurret(-5))
                .UNSTABLE_addTemporalMarkerOffset(1.8, () -> moveArm(350))
                .lineToLinearHeading(new Pose2d(12, -67, Math.toRadians(0)))


                .forward(30)
                .strafeLeft(25)
                .splineToLinearHeading(new Pose2d(68, -30, Math.toRadians(270)), Math.toRadians(0))

                .build();


        TrajectorySequence cycle1AlliancePark = drive.trajectorySequenceBuilder(startPose)

                .UNSTABLE_addTemporalMarkerOffset(0, () -> moveArm(800))
                .UNSTABLE_addTemporalMarkerOffset(1, () -> moveTurret(-100))
                .UNSTABLE_addTemporalMarkerOffset(2, () -> moveArm(350))
                .lineToLinearHeading(new Pose2d(-12,-37, Math.toRadians(0)))
                .UNSTABLE_addTemporalMarkerOffset(0.5, () -> intake.setPower(intakeSpeed))
                .waitSeconds(1.5)
                .UNSTABLE_addTemporalMarkerOffset(0, () -> intake.setPower(0))
                .UNSTABLE_addTemporalMarkerOffset(0, () -> moveArm(800))
                .UNSTABLE_addTemporalMarkerOffset(0.3, () -> moveTurretPower(-10, 0.5))
                .lineToLinearHeading(new Pose2d(8, -66, Math.toRadians(0)))
                .UNSTABLE_addTemporalMarkerOffset(0, () -> moveArm(350))
                .UNSTABLE_addTemporalMarkerOffset(0.45, () -> moveArm(0))

                .forward(32)
                .UNSTABLE_addTemporalMarkerOffset(0, () -> intake.setPower(-intakeSpeed))
                .UNSTABLE_addTemporalMarkerOffset(1.5, () -> intake.setPower(0))
                .splineTo(new Vector2d(52, -66), Math.toRadians(0),
                        SampleMecanumDrive.getVelocityConstraint(15, DriveConstants.MAX_ANG_VEL, DriveConstants.TRACK_WIDTH),
                        SampleMecanumDrive.getAccelerationConstraint(15)
                )
                .resetConstraints()

                .UNSTABLE_addTemporalMarkerOffset(0.5, () -> moveArm(500))
                .lineTo(new Vector2d(11,-67))
                .UNSTABLE_addTemporalMarkerOffset(0, () -> moveArm(1250))
                .UNSTABLE_addTemporalMarkerOffset(0.5, () -> moveTurret(-100))
                .lineToLinearHeading(new Pose2d(-2,-28, Math.toRadians(55)))
                .UNSTABLE_addTemporalMarkerOffset(0, () -> intake.setPower(intakeSpeed))
                .waitSeconds(1)
                .UNSTABLE_addTemporalMarkerOffset(0, () -> intake.setPower(0))
                .UNSTABLE_addTemporalMarkerOffset(0, () -> moveTurret(-15))
                .UNSTABLE_addTemporalMarkerOffset(1.8, () -> moveArm(350))
                .lineToLinearHeading(new Pose2d(12, -68, Math.toRadians(0)))
                .UNSTABLE_addTemporalMarkerOffset(0.35, () -> moveArm(0))

                .forward(35)
                .UNSTABLE_addTemporalMarkerOffset(0, () -> intake.setPower(-intakeSpeed))
                .UNSTABLE_addTemporalMarkerOffset(1.5, () -> intake.setPower(0))
                .splineTo(new Vector2d(58, -68), Math.toRadians(0),
                        SampleMecanumDrive.getVelocityConstraint(15, DriveConstants.MAX_ANG_VEL, DriveConstants.TRACK_WIDTH),
                        SampleMecanumDrive.getAccelerationConstraint(15)
                )
                .resetConstraints()

                .UNSTABLE_addTemporalMarkerOffset(0.5, () -> moveArm(500))
                .lineTo(new Vector2d(12,-68))
                .UNSTABLE_addTemporalMarkerOffset(0, () -> moveArm(1250))
                .UNSTABLE_addTemporalMarkerOffset(0.5, () -> moveTurret(-100))
                .lineToLinearHeading(new Pose2d(0,-28, Math.toRadians(55)))
                .UNSTABLE_addTemporalMarkerOffset(0, () -> intake.setPower(intakeSpeed))
                .waitSeconds(1)
                .UNSTABLE_addTemporalMarkerOffset(0, () -> intake.setPower(0))
                .UNSTABLE_addTemporalMarkerOffset(0, () -> moveTurret(-20))
                .UNSTABLE_addTemporalMarkerOffset(1.8, () -> moveArm(350))
                .lineToLinearHeading(new Pose2d(12, -69, Math.toRadians(0)))


                .forward(30)


                .build();

        TrajectorySequence cycle2AlliancePark = drive.trajectorySequenceBuilder(startPose)


                .UNSTABLE_addTemporalMarkerOffset(0, () -> moveArm(900))
                .UNSTABLE_addTemporalMarkerOffset(0.5, () -> moveTurret(-85))
                .lineToLinearHeading(new Pose2d(-2,-30, Math.toRadians(55)))
                .UNSTABLE_addTemporalMarkerOffset(0, () -> intake.setPower(intakeSpeed))
                .waitSeconds(1)
                .UNSTABLE_addTemporalMarkerOffset(0, () -> intake.setPower(0))
                .UNSTABLE_addTemporalMarkerOffset(0, () -> moveTurret(0))
                .UNSTABLE_addTemporalMarkerOffset(1.8, () -> moveArm(350))
                .lineToLinearHeading(alignmentPose)
                .UNSTABLE_addTemporalMarkerOffset(0.35, () -> moveArm(0))

                .forward(32)
                .UNSTABLE_addTemporalMarkerOffset(0, () -> intake.setPower(-intakeSpeed))
                .UNSTABLE_addTemporalMarkerOffset(1.5, () -> intake.setPower(0))
                .splineTo(new Vector2d(50, -64), Math.toRadians(0),
                        SampleMecanumDrive.getVelocityConstraint(15, DriveConstants.MAX_ANG_VEL, DriveConstants.TRACK_WIDTH),
                        SampleMecanumDrive.getAccelerationConstraint(15)
                )
                .resetConstraints()

                .UNSTABLE_addTemporalMarkerOffset(0.5, () -> moveArm(500))
                .lineToLinearHeading(new Pose2d(11,-66, Math.toRadians(0)))
                .UNSTABLE_addTemporalMarkerOffset(0, () -> moveArm(1250))
                .UNSTABLE_addTemporalMarkerOffset(0.5, () -> moveTurret(-100))
                .lineToLinearHeading(depositPose)
                .UNSTABLE_addTemporalMarkerOffset(0, () -> intake.setPower(intakeSpeed))
                .waitSeconds(1)
                .UNSTABLE_addTemporalMarkerOffset(0, () -> intake.setPower(0))
                .UNSTABLE_addTemporalMarkerOffset(0, () -> moveTurret(-20))
                .UNSTABLE_addTemporalMarkerOffset(1.8, () -> moveArm(350))
                .lineToLinearHeading(new Pose2d(12, -66, Math.toRadians(0)))
                .UNSTABLE_addTemporalMarkerOffset(0.35, () -> moveArm(0))


                .forward(35)
                .UNSTABLE_addTemporalMarkerOffset(0, () -> intake.setPower(-intakeSpeed))
                .UNSTABLE_addTemporalMarkerOffset(1.5, () -> intake.setPower(0))
                .splineTo(new Vector2d(54, -67), Math.toRadians(0),
                        SampleMecanumDrive.getVelocityConstraint(15, DriveConstants.MAX_ANG_VEL, DriveConstants.TRACK_WIDTH),
                        SampleMecanumDrive.getAccelerationConstraint(15)
                )
                .resetConstraints()

                .UNSTABLE_addTemporalMarkerOffset(0, () -> moveArm(500))
                .lineToLinearHeading(new Pose2d(12, -67, Math.toRadians(0)))
                .UNSTABLE_addTemporalMarkerOffset(0, () -> moveArm(1250))
                .UNSTABLE_addTemporalMarkerOffset(0.5, () -> moveTurret(-100))
                .lineToLinearHeading(depositPose)
                .UNSTABLE_addTemporalMarkerOffset(0, () -> intake.setPower(intakeSpeed))
                .waitSeconds(1)
                .UNSTABLE_addTemporalMarkerOffset(0, () -> intake.setPower(0))
                .UNSTABLE_addTemporalMarkerOffset(0, () -> moveTurret(-5))
                .UNSTABLE_addTemporalMarkerOffset(1.8, () -> moveArm(350))
                .lineToLinearHeading(new Pose2d(12, -67, Math.toRadians(0)))


                .forward(30)


                .build();

        TrajectorySequence cycle3AlliancePark = drive.trajectorySequenceBuilder(startPose)


                .UNSTABLE_addTemporalMarkerOffset(0, () -> moveArm(1250))
                .UNSTABLE_addTemporalMarkerOffset(0.5, () -> moveTurret(-100))
                .lineToLinearHeading(new Pose2d(-2,-24, Math.toRadians(55)))
                .UNSTABLE_addTemporalMarkerOffset(0, () -> intake.setPower(intakeSpeed))
                .waitSeconds(1)
                .UNSTABLE_addTemporalMarkerOffset(0, () -> intake.setPower(0))
                .UNSTABLE_addTemporalMarkerOffset(0, () -> moveTurret(0))
                .UNSTABLE_addTemporalMarkerOffset(1.8, () -> moveArm(350))
                .lineToLinearHeading(alignmentPose)
                .UNSTABLE_addTemporalMarkerOffset(0.35, () -> moveArm(0))

                .forward(32)
                .UNSTABLE_addTemporalMarkerOffset(0, () -> intake.setPower(-intakeSpeed))
                .UNSTABLE_addTemporalMarkerOffset(1.5, () -> intake.setPower(0))
                .splineTo(new Vector2d(50, -64), Math.toRadians(0),
                        SampleMecanumDrive.getVelocityConstraint(15, DriveConstants.MAX_ANG_VEL, DriveConstants.TRACK_WIDTH),
                        SampleMecanumDrive.getAccelerationConstraint(15)
                )
                .resetConstraints()


                .UNSTABLE_addTemporalMarkerOffset(0.5, () -> moveArm(500))
                .lineToLinearHeading(new Pose2d(11,-66, Math.toRadians(0)))
                .UNSTABLE_addTemporalMarkerOffset(0, () -> moveArm(1250))
                .UNSTABLE_addTemporalMarkerOffset(0.5, () -> moveTurret(-100))
                .lineToLinearHeading(depositPose)
                .UNSTABLE_addTemporalMarkerOffset(0, () -> intake.setPower(intakeSpeed))
                .waitSeconds(1)
                .UNSTABLE_addTemporalMarkerOffset(0, () -> intake.setPower(0))
                .UNSTABLE_addTemporalMarkerOffset(0, () -> moveTurret(-20))
                .UNSTABLE_addTemporalMarkerOffset(1.8, () -> moveArm(350))
                .lineToLinearHeading(new Pose2d(12, -66, Math.toRadians(0)))
                .UNSTABLE_addTemporalMarkerOffset(0.35, () -> moveArm(0))


                .forward(35)
                .UNSTABLE_addTemporalMarkerOffset(0, () -> intake.setPower(-intakeSpeed))
                .UNSTABLE_addTemporalMarkerOffset(1.5, () -> intake.setPower(0))
                .splineTo(new Vector2d(54, -66), Math.toRadians(0),
                        SampleMecanumDrive.getVelocityConstraint(15, DriveConstants.MAX_ANG_VEL, DriveConstants.TRACK_WIDTH),
                        SampleMecanumDrive.getAccelerationConstraint(15)
                )
                .resetConstraints()

                .UNSTABLE_addTemporalMarkerOffset(0, () -> moveArm(500))
                .lineToLinearHeading(new Pose2d(12,-67, Math.toRadians(0)))
                .UNSTABLE_addTemporalMarkerOffset(0, () -> moveArm(1250))
                .UNSTABLE_addTemporalMarkerOffset(0.5, () -> moveTurret(-100))
                .lineToLinearHeading(depositPose)
                .UNSTABLE_addTemporalMarkerOffset(0, () -> intake.setPower(intakeSpeed))
                .waitSeconds(1)
                .UNSTABLE_addTemporalMarkerOffset(0, () -> intake.setPower(0))
                .UNSTABLE_addTemporalMarkerOffset(0, () -> moveTurret(-5))
                .UNSTABLE_addTemporalMarkerOffset(1.8, () -> moveArm(350))
                .lineToLinearHeading(new Pose2d(12, -67, Math.toRadians(0)))


                .forward(30)


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

                drive.followTrajectorySequence(cycle1SharedPark);

             //   drive.followTrajectorySequence(cycle1AlliancePark);

                break;

            case MIDDLE:

                drive.followTrajectorySequence(cycle2SharedPark);

             //   drive.followTrajectorySequence(cycle2AlliancePark);

                break;

            case RIGHT:

                drive.followTrajectorySequence(cycle3SharedPark);

             //   drive.followTrajectorySequence(cycle3AlliancePark);



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

    public void moveTurretPower(int targetPosition, double speed) {
        turret.setTargetPosition(targetPosition);
        turret.setMode(DcMotor.RunMode.RUN_TO_POSITION);
        turret.setPower(speed);
    }


    public void intakeFreight() {

        if (colorSensor.getDistance(DistanceUnit.CM) <= 3.5) {

            intake.setPower(0);
            colorSensor2.enableLed(true);
        }
    }
}

