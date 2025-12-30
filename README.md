🎥 Face Proctoring System with GUI

A Face Proctoring System is a desktop-based application designed to ensure integrity during online examinations by monitoring candidates in real time using computer vision techniques. This system provides an interactive Graphical User Interface (GUI) and performs automated proctoring by analyzing webcam input.

The application continuously observes the examinee and detects suspicious behaviors such as multiple faces, absence from camera, and unauthorized movements, helping institutions conduct secure and fair online assessments.

🚀 Key Features

🧑 Real-time Face Detection using webcam feed

👥 Multiple Face Detection to identify impersonation or collaboration

❌ No Face / Camera Avoidance Detection

🖥️ User-friendly GUI for starting/stopping proctoring sessions

⚡ Lightweight & Efficient real-time processing

🛠️ Technologies Used

Python – Core programming language

OpenCV – Face detection and image processing

Haar Cascade Classifiers – Face detection model

Tkinter / PyQt – GUI development

NumPy – Array and image data handling

🖥️ System Workflow

User launches the application via the GUI

Webcam access is initialized

Live video frames are captured and processed

Face detection is performed on each frame

Violations (multiple/no face) are detected and logged

Proctoring session ends with saved logs

🎯 Use Cases

Online examinations

Remote assessments

Certification tests

University and college exams

Recruitment screening tests
