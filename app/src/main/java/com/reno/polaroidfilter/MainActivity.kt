package com.reno.polaroidfilter

import android.graphics.BitmapFactory
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val image1 = BitmapFactory.decodeResource(resources, R.drawable.polaroid2)
        val poraloid = TexturePoraloid(baseContext, image1)
        val image2 = BitmapFactory.decodeResource(resources, R.drawable.runa)
        val image = TextureImage(baseContext, image2)
        val renderer = ShapeRenderer(listOf(image, poraloid))

        gl_surface_view.setEGLContextClientVersion(2)
        gl_surface_view.setRenderer(renderer)
    }
}