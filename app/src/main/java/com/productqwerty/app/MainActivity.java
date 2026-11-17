package com.productqwerty.app;
import android.app.*;
import android.os.*;
import android.view.*;
import android.widget.*;
public class MainActivity extends Activity {
    public void onCreate(Bundle b) {
        super.onCreate(b);
        LinearLayout l = new LinearLayout(this);
        l.setOrientation(1);
        l.setPadding(30,60,30,30);
        l.setBackgroundColor(0xff10101c);
        TextView t = new TextView(this);
        t.setText("ProductQwerty\nГолосовой переводчик RU • ES • EN");
        t.setTextColor(-1);
        t.setTextSize(24);
        l.addView(t);
        TextView s = new TextView(this);
        s.setText("Перевод пока не подключён");
        s.setTextColor(-1);
        l.addView(s);
        Button start = new Button(this);
        start.setText("ЗАПУСТИТЬ");
        l.addView(start);
        Button stop = new Button(this);
        stop.setText("ОСТАНОВИТЬ");
        l.addView(stop);
        start.setOnClickListener(v -> {
            if (checkSelfPermission("android.permission.RECORD_AUDIO") != 0)
                requestPermissions(new String[]{"android.permission.RECORD_AUDIO"}, 1);
            else s.setText("Микрофон разрешён. Переводчик требует подключения сервиса.");
        });
        stop.setOnClickListener(v -> s.setText("Перевод остановлен"));
        setContentView(l);
    }
}
