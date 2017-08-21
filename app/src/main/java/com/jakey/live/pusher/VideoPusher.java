package com.jakey.live.pusher;

import android.graphics.ImageFormat;
import android.hardware.Camera;
import android.view.SurfaceHolder;

import com.jakey.live.params.VideoParams;

import java.util.Iterator;
import java.util.List;

/**
 * Created by jakey on 2017/7/25.
 */

@SuppressWarnings("deprecation")
public class VideoPusher extends BasePusher implements SurfaceHolder.Callback, Camera.PreviewCallback {

    private static final String TAG = VideoPusher.class.getSimpleName();
    private final PushNative mPushNative;
    private SurfaceHolder mSurfaceHolder;
    private VideoParams mVideoParams;
    private Camera mCamera;
    private byte[] buffers;
    private boolean isPushing = false;

    public VideoPusher(SurfaceHolder holder, VideoParams videoParams, PushNative pushNative) {
        mPushNative = pushNative;
        mSurfaceHolder = holder;
        mVideoParams = videoParams;
        mSurfaceHolder.addCallback(this);
    }

    @Override
    public void startPush() {
        mPushNative.setVideoOptions(
                mVideoParams.getWidth(),
                mVideoParams.getHeight(),
                mVideoParams.getBitRate(),
                mVideoParams.getFps());
        isPushing = true;
    }

    @Override
    public void stopPush() {
        isPushing = false;
    }

    @Override
    public void release() {
        stopPreview();
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        //SurfaceView初始化完成以后，开始初始化摄像头，并且进行预览
        startPreview();
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {

    }

    /**
     * 开始预览
     */
    private void startPreview() {
        try {
            //SurfaceView初始化完成，可以进行预览
            mCamera = Camera.open(mVideoParams.getCameraId());
            Camera.Parameters param = mCamera.getParameters();
            //设置预览图像的像素格式？？？？？？？？？NV-21
            param.setPreviewFormat(ImageFormat.NV21);


            //设置预览画面宽高 设置有问题先去掉了
//            param.setPreviewSize(mVideoParams.getWidth(), mVideoParams.getHeight());
            // 选择合适的预览尺寸
            List<Camera.Size> sizeList = param.getSupportedPreviewSizes();
            int height =0;
            int width = 0;

            // 如果sizeList只有一个我们也没有必要做什么了，因为就他一个别无选择
            if (sizeList.size() > 1) {
                Iterator<Camera.Size> itor = sizeList.iterator();
                while (itor.hasNext()) {
                    Camera.Size cur = itor.next();
                    if (cur.width >= width
                            && cur.height >= height) {
                        width = cur.width;
                        height = cur.height;
                        break;
                    }
                }
            }
            param.setPreviewSize(width, height);




            //设置预览帧频，但是x264压缩的时候还是有另外一个帧频的
            //param.setPreviewFpsRange(mVideoParams.getFps() - 1, mVideoParams.getFps());
            mCamera.setParameters(param);

            mCamera.setPreviewDisplay(mSurfaceHolder);

            //如果是正在直播的话需要实时获取预览图像数据
            //缓冲区，大小需要根据摄像头的分辨率而定，x4换算为字节
            buffers = new byte[mVideoParams.getWidth() * mVideoParams.getHeight() * 4];
            mCamera.addCallbackBuffer(buffers);
            mCamera.setPreviewCallbackWithBuffer(this);

            //开始预览
            mCamera.startPreview();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 停止预览
     */
    private void stopPreview() {
        if (mCamera != null) {
            mCamera.stopPreview();
            mCamera.release();
            mCamera = null;
        }
    }

    /**
     * 切换摄像头
     */
    public void switchCamera() {

        if (mVideoParams.getCameraId() == Camera.CameraInfo.CAMERA_FACING_BACK) {
            mVideoParams.setCameraId(Camera.CameraInfo.CAMERA_FACING_FRONT);
        } else {
            mVideoParams.setCameraId(Camera.CameraInfo.CAMERA_FACING_BACK);
        }

        //切换不同的摄像头需要停止，并且重新打开
        stopPreview();
        startPreview();
    }

    /**
     * 摄像头数据更新回调，获取摄像头的数据，并且推流
     *
     * @param data
     * @param camera
     */
    @Override
    public void onPreviewFrame(byte[] data, Camera camera) {
        if (mCamera != null) {
            mCamera.addCallbackBuffer(buffers);
        }

        if (isPushing) {
            mPushNative.fireVideo(data);
        }
    }
}
