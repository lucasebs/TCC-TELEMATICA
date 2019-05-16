package processor;

import org.opencv.core.*;
import org.opencv.core.Mat;
import org.opencv.core.MatOfRect;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.objdetect.CascadeClassifier;

import static org.opencv.imgproc.Imgproc.rectangle;

public class DetectFace {
    private CascadeClassifier faceDetector;
    private Integer numberOfFaces;

    public DetectFace() {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
        faceDetector = new CascadeClassifier("src/processor/haarcascade_frontalface_alt.xml");
    }

    public Integer getNumberOfFaces() {
        return this.numberOfFaces;
    }

    public Mat detection(Mat image) {
        MatOfRect faceDetections = new MatOfRect();
        faceDetector.detectMultiScale(image, faceDetections);

        this.numberOfFaces = faceDetections.toArray().length;

        // Draw a bounding box around each face.
        for (Rect rect : faceDetections.toArray()) {
            rectangle(image, new Point(rect.x, rect.y), new Point(rect.x + rect.width, rect.y + rect.height), new Scalar(0, 255, 0));
        }

        return image;
    }
}