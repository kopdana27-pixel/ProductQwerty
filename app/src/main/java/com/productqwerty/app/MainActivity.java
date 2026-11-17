package com.productqwerty.app;

import android.app.*;
import android.os.*;
import android.content.*;
import android.content.pm.PackageManager;
import android.speech.*;
import android.speech.tts.TextToSpeech;
import android.graphics.Color;
import android.view.*;
import android.widget.*;
import com.google.mlkit.nl.translate.*;
import java.util.*;

public class MainActivity extends Activity {
 Spinner source,target;
 TextView status,result;
 Button start,stop,swap;
 SpeechRecognizer recognizer;
 TextToSpeech tts;
 Translator translator;
 boolean active=false;
 String[] names={"Русский","Español","English"};
 String[] codes={"ru","es","en"};
 int lang(Spinner s){return Math.max(0,s.getSelectedItemPosition());}
 @Override public void onCreate(Bundle b){
  super.onCreate(b);
  getWindow().setStatusBarColor(Color.rgb(7,16,32));
  getWindow().setNavigationBarColor(Color.rgb(7,16,32));
  LinearLayout root=new LinearLayout(this); root.setOrientation(1); root.setPadding(24,35,24,20); root.setBackgroundColor(Color.rgb(7,16,32));
  TextView logo=new TextView(this); logo.setText("❄ ProductQwerty"); logo.setTextSize(29); logo.setTextColor(Color.rgb(56,189,248)); logo.setGravity(17); root.addView(logo);
  TextView sub=new TextView(this); sub.setText("ГОЛОСОВОЙ ПЕРЕВОДЧИК"); sub.setTextColor(Color.LTGRAY); sub.setGravity(17); sub.setPadding(0,8,0,28); root.addView(sub);
  source=new Spinner(this); target=new Spinner(this);
  ArrayAdapter<String> adapter=new ArrayAdapter<>(this,android.R.layout.simple_spinner_dropdown_item,names);
  source.setAdapter(adapter); target.setAdapter(adapter); source.setSelection(0); target.setSelection(1);
  addLabel(root,"Я говорю"); root.addView(source);
  swap=new Button(this); swap.setText("⇅  Поменять языки"); root.addView(swap);
  addLabel(root,"Переводить на"); root.addView(target);
  status=new TextView(this); status.setText("Готов к работе"); status.setTextColor(Color.WHITE); status.setGravity(17); status.setPadding(0,28,0,14); root.addView(status);
  result=new TextView(this); result.setText("Здесь появится распознанная и переведённая речь"); result.setTextColor(Color.rgb(190,220,240)); result.setTextSize(16); result.setGravity(17); result.setPadding(10,20,10,20); root.addView(result,new LinearLayout.LayoutParams(-1,0,1));
  start=new Button(this); start.setText("▶  НАЧАТЬ"); root.addView(start);
  stop=new Button(this); stop.setText("■  ОСТАНОВИТЬ"); root.addView(stop);
  setContentView(root);
  tts=new TextToSpeech(this, code->{});
  swap.setOnClickListener(v->{int a=source.getSelectedItemPosition();source.setSelection(target.getSelectedItemPosition());target.setSelection(a);});
  start.setOnClickListener(v->begin());
  stop.setOnClickListener(v->halt());
  if(android.os.Build.VERSION.SDK_INT>=23 && checkSelfPermission("android.permission.RECORD_AUDIO")!=PackageManager.PERMISSION_GRANTED) requestPermissions(new String[]{"android.permission.RECORD_AUDIO"},10);
 }
 void addLabel(LinearLayout r,String s){TextView t=new TextView(this);t.setText(s);t.setTextColor(Color.WHITE);t.setPadding(0,15,0,5);r.addView(t);}
 void begin(){
  if(checkSelfPermission("android.permission.RECORD_AUDIO")!=PackageManager.PERMISSION_GRANTED){requestPermissions(new String[]{"android.permission.RECORD_AUDIO"},10);return;}
  if(!SpeechRecognizer.isRecognitionAvailable(this)){status.setText("Распознавание речи недоступно на устройстве");return;}
  active=true; status.setText("Слушаю…"); listen();
 }
 void listen(){
  if(!active)return;
  if(recognizer!=null) recognizer.destroy();
  recognizer=SpeechRecognizer.createSpeechRecognizer(this);
  recognizer.setRecognitionListener(new RecognitionListener(){
   public void onReadyForSpeech(Bundle b){status.setText("Слушаю…");}
   public void onBeginningOfSpeech(){}
   public void onRmsChanged(float f){}
   public void onBufferReceived(byte[] b){}
   public void onEndOfSpeech(){}
   public void onError(int e){if(active){status.setText("Повторное подключение…");new Handler().postDelayed(()->listen(),900);}}
   public void onResults(Bundle b){
    ArrayList<String> x=b.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
    if(x!=null&&!x.isEmpty()) translate(x.get(0));
    else if(active)listen();
   }
   public void onPartialResults(Bundle b){}
   public void onEvent(int a,Bundle b){}
  });
  Intent i=new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
  i.putExtra(RecognizerIntent.EXTRA_LANGUAGE,codes[lang(source)]);
  i.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
  i.putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS,false);
  recognizer.startListening(i);
 }
 void translate(String words){
  int a=lang(source),b=lang(target);
  TranslatorOptions options=new TranslatorOptions.Builder().setSourceLanguage(codes[a]).setTargetLanguage(codes[b]).build();
  if(translator!=null)translator.close();
  translator=Translation.getClient(options);
  status.setText("Перевожу…");
  translator.downloadModelIfNeeded().addOnSuccessListener(v->translator.translate(words).addOnSuccessListener(out->{
   result.setText("Вы сказали: "+words+"\n\nПеревод: "+out);
   if(tts!=null){tts.setLanguage(new Locale(codes[b]));tts.speak(out,TextToSpeech.QUEUE_FLUSH,null,"translation");}
   if(active)new Handler().postDelayed(()->listen(),500);
  }).addOnFailureListener(e->failed())).addOnFailureListener(e->failed());
 }
 void failed(){status.setText("Не удалось перевести. Проверь интернет.");if(active)new Handler().postDelayed(()->listen(),1200);}
 void halt(){active=false;if(recognizer!=null)recognizer.cancel();status.setText("Остановлено");}
 @Override protected void onDestroy(){halt();if(recognizer!=null)recognizer.destroy();if(translator!=null)translator.close();if(tts!=null)tts.shutdown();super.onDestroy();}
}
