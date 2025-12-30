package com.myfirstapp.proctor;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.SwingConstants;

import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.Java2DFrameConverter;
import org.bytedeco.javacv.OpenCVFrameConverter;
import org.bytedeco.javacv.OpenCVFrameGrabber;
import org.bytedeco.opencv.opencv_core.Mat;
import org.bytedeco.opencv.opencv_core.Point;
import org.bytedeco.opencv.opencv_core.Rect;
import org.bytedeco.opencv.opencv_core.RectVector;
import org.bytedeco.opencv.opencv_core.Scalar;
import org.bytedeco.opencv.opencv_core.Size;
import org.bytedeco.opencv.opencv_objdetect.CascadeClassifier;
import static org.bytedeco.opencv.global.opencv_imgproc.*;
import static org.bytedeco.opencv.global.opencv_core.*;


public class MainApp {

    public static void main(String[] args) {
        JFrame window = new JFrame("Face Proctoring System");
        window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        window.setSize(800, 600);
        window.setLayout(new BorderLayout());

        JLabel videoPanel = new JLabel();
        videoPanel.setHorizontalAlignment(SwingConstants.CENTER);
        window.add(videoPanel, BorderLayout.CENTER);

        JLabel statusLabel = new JLabel("Status: Initializing...");
        statusLabel.setHorizontalAlignment(SwingConstants.CENTER);
        statusLabel.setFont(new Font("Serif", Font.BOLD, 20));
        window.add(statusLabel, BorderLayout.SOUTH);

        window.setVisible(true);

        new Thread(new Runnable() {
            @Override
            public void run() {
                String modelPath = "C:\\Users\\karth\\eclipse-workspace\\face-proctoring-system\\";

                CascadeClassifier frontalFaceCascade = new CascadeClassifier(modelPath + "haarcascade_frontalface_alt.xml");
                CascadeClassifier eyeCascade = new CascadeClassifier(modelPath + "haarcascade_eye.xml");
                
                OpenCVFrameGrabber grabber = new OpenCVFrameGrabber(0);
                Clip alarmClip = null;
                boolean isRinging = false;
                PrintWriter logWriter = null;
                DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");

                try {
                    logWriter = new PrintWriter(new FileWriter(modelPath + "log.txt", true));
                    File soundFile = new File(modelPath + "alert.wav");
                    AudioInputStream audioIn = AudioSystem.getAudioInputStream(soundFile);
                    alarmClip = AudioSystem.getClip();
                    alarmClip.open(audioIn);
                } catch (Exception e) {
                    System.err.println("Error initializing resources.");
                    e.printStackTrace();
                }

                try {
                    grabber.start();
                    Java2DFrameConverter converter2D = new Java2DFrameConverter();
                    OpenCVFrameConverter.ToMat converterMat = new OpenCVFrameConverter.ToMat();

                    while (true) {
                        Frame frame = grabber.grab();
                        if (frame == null) break;

                        Mat image = converterMat.convert(frame);
                        Mat grayImage = new Mat();
                        // --- CORRECTED TYPO HERE ---
                        cvtColor(image, grayImage, COLOR_BGR2GRAY);
                        equalizeHist(grayImage, grayImage);

                        RectVector detectedEyes = new RectVector();
                        eyeCascade.detectMultiScale(grayImage, detectedEyes, 1.1, 15, 0, new Size(25, 25), new Size());
                        long numPeople = Math.round(detectedEyes.size() / 2.0);

                        String warningMessage = "";
                        if (numPeople != 1) {
                            warningMessage = numPeople == 0 ? "WARNING: No Person Detected!" : "WARNING: Multiple People Detected!";
                        }

                        if (!warningMessage.isEmpty()) {
                            statusLabel.setText(warningMessage);
                            statusLabel.setForeground(Color.RED);
                            if (!isRinging) {
                                if (logWriter != null) logWriter.println("[" + dtf.format(LocalDateTime.now()) + "] " + warningMessage);
                                if (alarmClip != null) alarmClip.loop(Clip.LOOP_CONTINUOUSLY);
                                else Toolkit.getDefaultToolkit().beep();
                                isRinging = true;
                            }
                        } else {
                            statusLabel.setText("Status: OK");
                            statusLabel.setForeground(new Color(0, 150, 0));
                            if (isRinging) {
                                if (alarmClip != null) alarmClip.stop();
                                isRinging = false;
                            }
                        }
                        
                        RectVector frontalFaces = new RectVector();
                        frontalFaceCascade.detectMultiScale(grayImage, frontalFaces);
                        Scalar faceBoxColor = warningMessage.isEmpty() ? new Scalar(0, 255, 0, 0) : new Scalar(0, 0, 255, 0);
                        for (long i = 0; i < frontalFaces.size(); i++) {
                            Rect r = frontalFaces.get(i);
                            rectangle(image, new Point(r.x(), r.y()), new Point(r.x() + r.width(), r.y() + r.height()), faceBoxColor, 2, 0, 0);
                        }
                        
                        videoPanel.setIcon(new ImageIcon(converter2D.convert(converterMat.convert(image))));
                    }
                    grabber.stop();
                    if (alarmClip != null) alarmClip.close();
                    if (logWriter != null) logWriter.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }
}