/*
 * Copyright 2014 Ankush Sachdeva
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package github.ankushsachdeva.emojicon;

import com.karabow.zing.EmojiDisplay;
import com.karabow.zing.KeyboardListener;
import com.karabow.zing.Status;
import com.karabow.zing.ZingIdEditActivity;
import com.karabow.zing.R;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.widget.EditText;

/**
 * @author Hieu Rocker (rockerhieu@gmail.com).
 */
public class EmojiconEditText extends EditText 
{
    private int mEmojiconSize;
    private KeyboardListener keyl;

    public EmojiconEditText(Context context) {
        super(context);
        mEmojiconSize = (int) getTextSize();

    }

    public EmojiconEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs);
    }

    public EmojiconEditText(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(attrs);
    }

    private void init(AttributeSet attrs) {
        TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.Emojicon);
        mEmojiconSize = (int) a.getDimension(R.styleable.Emojicon_emojiconSize, getTextSize());
        a.recycle();
        setText(getText());
    }

    @Override
    protected void onTextChanged(CharSequence text, int start, int lengthBefore, int lengthAfter) 
    {
        EmojiconHandler.addEmojis(getContext(), getText(), mEmojiconSize);
    }

    /**
     * Set the size of emojicon in pixels.
     */
    public void setEmojiconSize(int pixels)
    {
        mEmojiconSize = pixels;
    }
    
    /**
     * set a listener to monitor KeyboardVisibility
     */
    public void setKeyboardListener(KeyboardListener kl)
    {
    	keyl = kl;
    }
    
    @Override
    public boolean onKeyPreIme(int keycode, KeyEvent ke)
    {
    	if(keycode == KeyEvent.KEYCODE_BACK && ke.getAction() == KeyEvent.ACTION_UP)
    	{
    		if(EmojiDisplay.isEmojiVisible() && keyl != null)
    		{
    			keyl.isBackKeyPressed(true);
    			return true;
    		}
    		
    		else if(!EmojiDisplay.isEmojiVisible())
    		{
    			ZingIdEditActivity.keyboardVisible = false;
                Status.keyboardVisible = false;
                return false;
    		}
    	}
    	return false;
    }
    
}
