package Extra

import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import com.bitvale.lightprogress.LightProgress
import com.blucor.aoneenterprises.R


/**
 * Created by admin on 4/9/2016.
 */
class CustomLoader : Dialog {
    constructor(context: Context?) : super(context!!) {

    // TODO Auto-generated constructor stub
    }
    constructor(context: Context, theme: Int) : super(context!!, theme) {
        // TODO Auto-generated constructor stub
         setContentView(R.layout.loader);

//        setContentView(R.layout.loader)
//        var dialog = CustomLoader(context)
//
        var light : LightProgress= findViewById(R.id.light)
         light.on()


        /*  new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    try {
                        Thread.sleep(1000);
                        new Handler().post(new Runnable() {
                            @Override
                            public void run() {
                                // Write your code here to update the UI.
                                showPopup();
                                startStop();
                            }
                        });
                    } catch (Exception e) {

                    }
                }
            }
        }).start();*/
    }

    constructor(
        context: Context?, cancelable: Boolean,
        cancelListener: DialogInterface.OnCancelListener?
    ) : super(
        context!!,
        cancelable,
        cancelListener
    ) {        // TODO Auto-generated constructor stub
    }
}