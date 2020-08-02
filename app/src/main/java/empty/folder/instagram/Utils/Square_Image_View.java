package empty.folder.instagram.Utils;

import android.content.Context;
import android.util.AttributeSet;

import androidx.appcompat.widget.AppCompatImageButton;
import androidx.appcompat.widget.AppCompatImageView;

public class Square_Image_View extends AppCompatImageView {

    public Square_Image_View(Context context) {
        super(context);
    }

    public Square_Image_View(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public Square_Image_View(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, widthMeasureSpec);
    }
}
