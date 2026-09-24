package com.example.gen3saveinspector;

import android.app.*;
import android.os.Bundle;
import android.content.*;
import android.net.Uri;
import android.widget.*;
import java.io.*;
import java.util.*;
import android.graphics.Typeface;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.text.style.TypefaceSpan;

public class MainActivity extends Activity {
    private static final int REQ_OPEN = 1001;
    private TextView status;
    private LinearLayout list;
    private Gen3SaveParser.Result current;

    @Override public void onCreate(Bundle b) {
        super.onCreate(b);
        buildUi();
    }
    private CharSequence formatPokemonSummary(Gen3SaveParser.Pokemon p) {

    SpannableStringBuilder s = new SpannableStringBuilder();

    // 位置
    s.append(p.location).append("  ");

    // 宝可梦名字
    int nameStart = s.length();
    s.append(p.species);
    int nameEnd = s.length();

    // 名字加粗
    s.setSpan(
            new StyleSpan(Typeface.BOLD),
            nameStart,
            nameEnd,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
    );

    // 性格
    s.append("  ").append(p.nature).append("\n");

    // 从这里开始是 IV / EV 数据
    int statsStart = s.length();

    s.append("IV  ")
            .append(p.ivs.compact())
            .append("\n");

    s.append("EV  ")
            .append(p.evs.compact());

    int statsEnd = s.length();

    // CMD / Courier 风格的等宽字体
    s.setSpan(
            new TypefaceSpan("monospace"),
            statsStart,
            statsEnd,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
    );

    // 深蓝色
    s.setSpan(
            new ForegroundColorSpan(0xFF17365D),
            statsStart,
            statsEnd,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
    );

    return s;
}

    private int dp(int v) {
        return (int)(v * getResources().getDisplayMetrics().density + 0.5f);
    }

    private TextView label(String s, float sp) {
        TextView t = new TextView(this);
        t.setText(s);
        t.setTextSize(sp);
        t.setTextColor(0xFF111827);
        t.setPadding(dp(12),dp(8),dp(12),dp(8));
        return t;
    }

    private void buildUi() {
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(dp(10),dp(10),dp(10),dp(10));
        root.setBackgroundColor(0xFFF7F8FA);

        TextView title = label("Gen III Save Inspector",24);
        title.setTypeface(null,1);
        root.addView(title);

        TextView sub = label("读取第三世代 .sav/.srm：性格、IV、EV、队伍与14个箱子。只读，不修改存档。",14);
        sub.setTextColor(0xFF4B5563);
        root.addView(sub);

        Button open = new Button(this);
        open.setText("选择 .sav / .srm 存档");
        open.setOnClickListener(v -> chooseFile());
        root.addView(open, new LinearLayout.LayoutParams(-1,-2));

        status = label("尚未选择存档",13);
        status.setTextColor(0xFF374151);
        root.addView(status);

        ScrollView scroll = new ScrollView(this);
        list = new LinearLayout(this);
        list.setOrientation(LinearLayout.VERTICAL);
        scroll.addView(list, new ScrollView.LayoutParams(-1,-2));
        root.addView(scroll, new LinearLayout.LayoutParams(-1,0,1f));

        setContentView(root);
    }

    private void chooseFile() {
        Intent i = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        i.addCategory(Intent.CATEGORY_OPENABLE);
        i.setType("*/*");
        startActivityForResult(i,REQ_OPEN);
    }

    @Override protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode,resultCode,data);
        if(requestCode==REQ_OPEN && resultCode==RESULT_OK && data!=null && data.getData()!=null) {
            Uri uri=data.getData();
            try {
                final int flags = data.getFlags() & (Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                try { getContentResolver().takePersistableUriPermission(uri, flags & Intent.FLAG_GRANT_READ_URI_PERMISSION); } catch(Exception ignored) {}
                loadUri(uri);
            } catch(Exception e) {
                showError(e);
            }
        }
    }

    private byte[] readAll(InputStream in) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        byte[] buf = new byte[8192];
        int n;
        while((n=in.read(buf))!=-1) out.write(buf,0,n);
        return out.toByteArray();
    }

    private void loadUri(Uri uri) {
        list.removeAllViews();
        status.setText("正在读取…");
        try(InputStream in=getContentResolver().openInputStream(uri)) {
            if(in==null) throw new IOException("无法打开文件");
            byte[] data=readAll(in);
            current=Gen3SaveParser.parse(data);
            status.setText(current.info() + (data.length>Gen3SaveParser.SAVE_SIZE ? " · 已忽略尾部RTC/元数据" : ""));
            render(current.pokemon);
        } catch(Exception e) {
            showError(e);
        }
    }

    private void render(List<Gen3SaveParser.Pokemon> mons) {
        for(int idx=0; idx<mons.size(); idx++) {
            Gen3SaveParser.Pokemon p=mons.get(idx);
            TextView row=label(String.format(Locale.US,"%03d  %s",idx+1,p.summary()),15);
            row.setText(formatPokemonSummary(p));
            row.setBackgroundColor(0xFFFFFFFF);
            row.setPadding(dp(12),dp(10),dp(12),dp(10));
            LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,-2);
            lp.setMargins(0,0,0,dp(6));
            row.setOnClickListener(v -> new AlertDialog.Builder(this)
                .setTitle(p.species)
                .setMessage(p.detail())
                .setPositiveButton("关闭",null)
                .show());
            list.addView(row,lp);
        }
    }

    private void showError(Exception e) {
        status.setText("解析失败");
        new AlertDialog.Builder(this)
            .setTitle("无法读取存档")
            .setMessage(e.getMessage()==null ? e.toString() : e.getMessage())
            .setPositiveButton("确定",null)
            .show();
    }
}
