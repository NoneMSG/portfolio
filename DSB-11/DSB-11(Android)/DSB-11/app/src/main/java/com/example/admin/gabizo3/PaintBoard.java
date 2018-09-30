package com.example.admin.gabizo3;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import java.util.ArrayList;
import java.util.Stack;
import android.os.Handler;

public class PaintBoard extends View {

    DrawActivity drawActivity = new DrawActivity();
    Stack undos = new Stack(); // 비트맵을 저장하는 컨테이너
    public static int maxUndos = 10; // 비트맵의 최고 개수
    Canvas mCanvas;
    Bitmap mBitmap;
    final Paint mPaint;
    float lastX; // 화면 x 좌표
    float lastY; // 화면 y 좌표
    float realX; // 실제 x 좌표
    float realY; // 실제 y 좌표
    ArrayList<point> list = new ArrayList<point>(); // 점을 저장하는 list
    Stack undoNum = new Stack(); // 각 라인의 좌표의 개수 저장
    static int totalPointNum = 0;

    private static final boolean RENDERING_ANTIALIAS = true;
    private static final boolean DITHER_FLAG = true;

    static final float XRATE = (float)20.00; // 실제 좌표 x 1개당 화면 좌표 x의 개수
    static final float YRATE = (float)17.50; // 실제 좌표 y 1개당 화면 좌표 y의 개수

    static final float XRANGE = (float)35.00; // 화면 x 길이 -35 , 35
    static final float YRANGE = (float)113.00;// 화면 y 길이 113 , 65

    // point의 시작,끝 표시
    static final int START_MARK = 1;
    static final int FINISH_MARK = 2;

    //Paint객체의 정보
    private int mCertainColor = 0xFF000000;
    private float mStrokeWidth = 6.0f;
    private Handler mHandler;

    public PaintBoard(Context context, Handler h) {
        super(context);

        mHandler = h;
        // Paint 객체 생성
        mPaint = new Paint();
        mPaint.setAntiAlias(RENDERING_ANTIALIAS); //Paint 객체의 경계면을 부드럽게 처리할지
        mPaint.setColor(mCertainColor);//Paint 객체의 컬러
        mPaint.setStyle(Paint.Style.STROKE);//Paint 객체의 스타일
        mPaint.setStrokeJoin(Paint.Join.ROUND); //Paint 객체의 끝 모양(각지게, 둥글게)
        mPaint.setStrokeCap(Paint.Cap.ROUND); //Paint 객체의 끝나는 지점 모양 설정
        mPaint.setStrokeWidth(mStrokeWidth);//Paint 객체의 굵기
        mPaint.setDither(DITHER_FLAG); //이미지보다 장비의 표현력이 떨어질때 이미지의 색상을 낮춤 ( true일 경우)


        lastX = -1;
        lastY = -1;

        Log.d("PaintBoard", "initialized.");

    }


    // 화면 초기화
    public void clearBoard(int width, int height)
    {
        Bitmap img = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas();
        canvas.setBitmap(img);

        mBitmap = img;
        mCanvas = canvas;

        drawBackground(mCanvas);

        invalidate();

        //Undo를 위한 모든 Bitmap 제거
        while(undos.size() != 0) {
            Bitmap pic = (Bitmap)undos.pop();
            // Bitmap 메모리 제거
            pic.recycle();
            undos.remove(pic);
        }
        totalPointNum = 0;
        mHandler.sendEmptyMessage(2);
        Log.d("PaintBoard", "clearBoard() called.");
    }


    // Undo를 위한 Bitmap 저장
    public void saveUndo()
    {
        if (mBitmap == null) return;
        while (undos.size() >= maxUndos){
            Bitmap i = (Bitmap)undos.get(undos.size()-1);//
            i.recycle();
            undos.remove(i);
        }

        Bitmap img = Bitmap.createBitmap(mBitmap.getWidth(), mBitmap.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas();
        canvas.setBitmap(img);
        canvas.drawBitmap(mBitmap, 0, 0, mPaint);

        undos.push(img);

        Log.d("PaintBoard", "saveUndo() called.");
    }

    // 취소 기능
    public void undo()
    {
        Bitmap prev = null;

        try {
            prev = (Bitmap)undos.pop();
        } catch(Exception ex) {
            Log.e("PaintBoard", "Exception : " + ex.getMessage());
        }

        if (prev != null){
            drawBackground(mCanvas);
            mCanvas.drawBitmap(prev, 0, 0, mPaint);
            invalidate();

            if(undoNum.size() != 0) {
                int ptNum = (int) undoNum.pop();

                int j = 0;
                for (int i = totalPointNum - ptNum; j < ptNum; ++j) {

                    list.remove(i);
                }
                totalPointNum = list.size();
            }
            prev.recycle();
        }

        mHandler.sendEmptyMessage(2);
        Log.d("PaintBoard", "undo() called.");
    }

    // 배경 그리기
    public void drawBackground(Canvas canvas)
    {
        String bg = "#00FFFFFF";
        Log.d("PaintBoard","drawBackground");
        if (canvas != null) {
            canvas.drawColor(Color.WHITE);
        }
    }

    // 비트맵 생성
    public void newImage(int width, int height)
    {
        Log.d("PaintBoard","newImage");
        Bitmap img = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        //Bitmap.Config.ARGB_8888가 아닌 Bitmap.Config.RGB_565를 사용할 경우 원치않은 배경색이 나타 날수 있다. Bitmap.Config.ARGB_8888는 투명도까지 저정할수 있다.
        Canvas canvas = new Canvas();
        canvas.setBitmap(img);

        mBitmap = img;
        mCanvas = canvas;

        drawBackground(mCanvas);

        invalidate();
    }


    // 화면이 변할 때 호출
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {

        Log.d("PaintBoard","onSizeChaged");
        if (w > 0 && h > 0) {
            newImage(w, h);
        }
    }

    // 무효화영역이 생기거나 그림이 새로 그려질경우 호출됨
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        Log.d("PaintBoard", "onDraw");
        if (mBitmap != null) {
            canvas.drawBitmap(mBitmap, 0, 0, null);
            Log.d("PaintBoard", "onDraw2");
        }

    }

    // 화면을 터치 하였을 경우의 메소드
    public boolean onTouchEvent(MotionEvent event) {
        int action = event.getAction();

        float x = event.getX();
        float y = event.getY();

        switch (action) {

            case MotionEvent.ACTION_UP:

                if (lastX != -1) {
                    mCanvas.drawLine(lastX, lastY, x, y, mPaint);
                }

                lastX = x;
                lastY = y;

                realX = (lastX/XRATE)-XRANGE;
                realY = YRANGE-(lastY/YRATE);

                point pt = new point(realX,realY);
                pt.lastmark = FINISH_MARK;
                list.add(pt);
                int pointNum = list.size()-totalPointNum;
                Log.d("메시지", "각 라인의 좌표 개수 : " + pointNum);
                undoNum.push(pointNum);
                totalPointNum += pointNum;

                Log.d("메시지","총 좌표 개수 : "+totalPointNum);

                // reset coordinates
                lastX = -1;
                lastY = -1;

                mHandler.sendEmptyMessage(2);
                break;

            case MotionEvent.ACTION_DOWN:
                saveUndo();
                mHandler.sendEmptyMessage(1);
                // draw line with the coordinates
                if (lastX != -1) {
                    if (x != lastX || x != lastY) {
                        mCanvas.drawLine(lastX, lastY, x, y, mPaint);
                    }
                }

                // set the last coordinates
                lastX = x;
                lastY = y;

                realX = (lastX/XRATE)-XRANGE;
                realY = YRANGE-(lastY/YRATE);

                pt = new point(realX,realY);
                pt.lastmark = START_MARK;
                list.add(pt);

                break;

            case MotionEvent.ACTION_MOVE:

                if (lastX != -1) {
                    mCanvas.drawLine(lastX, lastY, x, y, mPaint);
                }

                lastX = x;
                lastY = y;

                realX = (lastX/XRATE)-XRANGE;
                realY = YRANGE-(lastY/YRATE);

                pt = new point(realX,realY);
                list.add(pt);

                break;
        }

        invalidate();
        return true;
    }
}
