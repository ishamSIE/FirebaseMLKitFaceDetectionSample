package com.devlomi.mlkitfacedetection

import android.content.Intent
import android.graphics.*
import android.os.Bundle
import android.provider.MediaStore
import android.support.v7.app.AppCompatActivity
import com.google.firebase.ml.vision.FirebaseVision
import com.google.firebase.ml.vision.common.FirebaseVisionImage
import com.google.firebase.ml.vision.face.FirebaseVisionFace
import com.google.firebase.ml.vision.face.FirebaseVisionFaceDetectorOptions
import com.google.firebase.ml.vision.face.FirebaseVisionFaceLandmark
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity() {


    companion object {
        val PICK_IMG_REQUEST = 8729
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        button.setOnClickListener {
            pickImageFromGallery()
        }


    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == PICK_IMG_REQUEST && resultCode == RESULT_OK) {
            val uri = data?.data

            val pickedImageBitmap = MediaStore.Images.Media.getBitmap(this.contentResolver, uri)

            detectFace(pickedImageBitmap)

        }
    }

    fun detectFace(pickedImageBitmap: Bitmap) {

        val visionImage = FirebaseVisionImage.fromBitmap(pickedImageBitmap)

        val options = FirebaseVisionFaceDetectorOptions.Builder()
                .setModeType(FirebaseVisionFaceDetectorOptions.ACCURATE_MODE)
                .setLandmarkType(FirebaseVisionFaceDetectorOptions.ALL_LANDMARKS)
                .build()

        val detector = FirebaseVision.getInstance()
                .getVisionFaceDetector(options)


        val eyeBitmap = BitmapFactory.decodeResource(resources, R.drawable.cartoon_eye)
        val mouthBitmap = BitmapFactory.decodeResource(resources, R.drawable.mouth)

        detector.detectInImage(visionImage).addOnSuccessListener {


            val newBitmap = pickedImageBitmap.copy(Bitmap.Config.ARGB_8888, true)
            val canvas = Canvas(newBitmap)


            it.forEach {

                val rightEye = it.getLandmark(FirebaseVisionFaceLandmark.RIGHT_EYE)
                val leftEye = it.getLandmark(FirebaseVisionFaceLandmark.LEFT_EYE)



                val eyeSize = (it.boundingBox.width()) * 0.15f

                if (leftEye != null) {
                    drawEye(eyeBitmap, canvas, leftEye, eyeSize)
                }
                if (rightEye != null) {
                    drawEye(eyeBitmap, canvas, rightEye, eyeSize)
                }


                drawMouth(canvas, mouthBitmap, it)
            }

            image_view.setImageBitmap(newBitmap)

        }

    }

    fun drawLandmark(canvas: Canvas, landmark: FirebaseVisionFaceLandmark?) {
        if (landmark != null) {
            canvas.drawCircle(landmark.position.x, landmark.position.y, 15f, getPaint())
        }
    }

    private fun drawEye(eyeBitmap: Bitmap, canvas: Canvas, eye: FirebaseVisionFaceLandmark, eyeSize: Float) {
        canvas.save()
        canvas.translate(eye.position.x, eye.position.y)
        var rectF = RectF(-eyeSize, -eyeSize,
                eyeSize, eyeSize)
        canvas.drawBitmap(eyeBitmap, null, rectF, null)
        canvas.restore()

    }

    private fun pickImageFromGallery() {
        val photoPickerIntent = Intent(Intent.ACTION_PICK)
        photoPickerIntent.type = "image/*"
        startActivityForResult(photoPickerIntent, PICK_IMG_REQUEST)
    }


    private fun drawMouth(canvas: Canvas, mouthBitmap: Bitmap, face: FirebaseVisionFace) {
        val leftMouth = face.getLandmark(FirebaseVisionFaceLandmark.LEFT_MOUTH)
        val rightMouth = face.getLandmark(FirebaseVisionFaceLandmark.RIGHT_MOUTH)
        val bottomMouth = face.getLandmark(FirebaseVisionFaceLandmark.BOTTOM_MOUTH)
        val bottomNose = face.getLandmark(FirebaseVisionFaceLandmark.NOSE_BASE)



        if (leftMouth != null && rightMouth != null && bottomMouth != null && bottomNose != null) {
            val mouthSizeWidth = leftMouth.position.x - rightMouth.position.x
            val mouthSizeHeight = bottomMouth.position.y - bottomNose.position.y
            val centreX = mouthSizeWidth / 2 + rightMouth.position.x
            val centreY = mouthSizeHeight / 2 + bottomNose.position.y + 20

            canvas.save()
            canvas.translate(centreX, centreY)
            val rectF = RectF(-mouthSizeWidth / 2f * 1.5f, -mouthSizeHeight / 2f * 1.5f,
                    mouthSizeWidth / 2f * 1.5f, mouthSizeHeight / 2f * 1.5f)


            canvas.drawBitmap(mouthBitmap, null, rectF, null)
            canvas.restore()

        }
    }

    private fun getPaint(): Paint {
        val p = Paint()
        p.setStyle(Paint.Style.STROKE)
        p.setAntiAlias(true)
        p.setFilterBitmap(true)
        p.setDither(true)
        p.setColor(Color.RED)
        return p
    }
}
