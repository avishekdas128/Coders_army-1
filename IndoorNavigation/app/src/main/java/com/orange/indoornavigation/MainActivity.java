package com.orange.indoornavigation;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Point;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.widget.Toast;

import com.google.ar.core.Anchor;
import com.google.ar.core.Frame;
import com.google.ar.core.HitResult;
import com.google.ar.core.Plane;
import com.google.ar.core.Trackable;
import com.google.ar.sceneform.AnchorNode;
import com.google.ar.sceneform.Scene;
import com.google.ar.sceneform.rendering.ModelRenderable;
import com.google.ar.sceneform.ux.ArFragment;
import com.google.ar.sceneform.ux.TransformableNode;

import java.util.List;


public class MainActivity extends AppCompatActivity {

    private ArFragment fragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        fragment = (ArFragment) getSupportFragmentManager().findFragmentById(R.id.sceneform_fragment);
        fragment.getArSceneView().getScene().addOnUpdateListener(updateListener);
    }

    private Scene.OnUpdateListener updateListener = frameTime -> {
        Frame frame = fragment.getArSceneView().getArFrame();
        if(frame == null) {
            return;
        }
        for(Plane plane : frame.getUpdatedTrackables(Plane.class)) {
            addObjectModel(Uri.parse("model.sfb"));
            break;
        }
    };

    private void addObjectModel(Uri object) {
        Frame frame = fragment.getArSceneView().getArFrame();
        Point center = getScreenCenter();
        fragment.getArSceneView().getScene()
                .removeOnUpdateListener(updateListener);
        if(frame != null) {
            List<HitResult> result = frame.hitTest(center.x, center.y);
            for(HitResult hit : result) {
                Trackable trackable = hit.getTrackable();
                if (trackable instanceof Plane && ((Plane) trackable).isPoseInPolygon(hit.getHitPose())) {
                    placeObject(hit.createAnchor(), object);
                    break;
                }
            }
        }
    }

    private Point getScreenCenter() {
        if(fragment == null || fragment.getView() == null) {
            return new android.graphics.Point(0,0);
        }
        int w = fragment.getView().getWidth()/2;
        int h = fragment.getView().getHeight()/2;
        return new android.graphics.Point(w, h);
    }

    private void placeObject(Anchor anchor, Uri object) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            ModelRenderable.builder()
                    .setSource(fragment.getContext(), object)
                    .build()
                    .thenAccept(modelRenderable -> addNodeToScene(anchor, modelRenderable, object))
                    .exceptionally(throwable -> null);
        }
    }

    private void addNodeToScene(Anchor createAnchor, ModelRenderable renderable, Uri object) {
        AnchorNode anchorNode = new AnchorNode(createAnchor);
        TransformableNode transformableNode = new TransformableNode(fragment.getTransformationSystem());
        transformableNode.setName(object.toString());
        transformableNode.setRenderable(renderable);
        transformableNode.setParent(anchorNode);
        fragment.getArSceneView().getScene().addChild(anchorNode);
        transformableNode.setOnTapListener((hitTestResult, motionEvent) -> {
            Toast.makeText(this, "model clicked", Toast.LENGTH_SHORT).show();
        });
        transformableNode.select();
    }
}
