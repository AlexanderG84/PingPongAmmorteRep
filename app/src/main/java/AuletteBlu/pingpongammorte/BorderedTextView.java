package AuletteBlu.pingpongammorte;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.widget.TextView;

public class BorderedTextView extends androidx.appcompat.widget.AppCompatTextView {

    private int borderColor = Color.BLACK; // Colore del bordo
    private float borderWidth = 4; // Larghezza del bordo

    public BorderedTextView(Context context) {
        super(context);
    }

    public BorderedTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public BorderedTextView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        final ColorStateList textColor = getTextColors();

        // Disegna il testo con il colore del bordo
        getPaint().setStyle(Paint.Style.STROKE);
        getPaint().setStrokeWidth(borderWidth);
        setTextColor(borderColor);
        super.onDraw(canvas);

        // Disegna il testo con il colore principale
        getPaint().setStyle(Paint.Style.FILL);
        setTextColor(textColor);
        super.onDraw(canvas);
    }
}

