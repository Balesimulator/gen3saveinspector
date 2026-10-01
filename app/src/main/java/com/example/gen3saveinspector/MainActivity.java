package com.example.gen3saveinspector;

import android.app.*;
import android.content.*;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.text.style.TypefaceSpan;
import android.view.Gravity;
import android.widget.*;

import java.io.*;
import java.util.*;

public class MainActivity extends Activity {
    private static final int REQ_OPEN = 1001;
    private static final int IV_FILTER_NONE = 0;
    private static final int IV_FILTER_ANY = 1;
    private static final int IV_FILTER_HP = 2;
    private static final int IV_FILTER_ATK = 3;
    private static final int IV_FILTER_DEF = 4;
    private static final int IV_FILTER_SPA = 5;
    private static final int IV_FILTER_SPD = 6;
    private static final int IV_FILTER_SPE = 7;
    private TextView status;
    private LinearLayout list;
    private Button filterButton;
    private Gen3SaveParser.Result current;
    private String sourceStatus = "";
    private String selectedNature;
    private Integer selectedSpeciesId;
    private int selectedIvFilter = IV_FILTER_NONE;

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

    s.setSpan(
            new StyleSpan(Typeface.BOLD),
            nameStart,
            nameEnd,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
    );

    // 性格
    s.append("  ").append(p.nature).append("\n");

    // IV
    appendStatsLine(s, "IV  ", p.ivs.compact());

    s.append("\n");

    // EV
    appendStatsLine(s, "EV  ", p.evs.compact());

    return s;
}
    private void appendStatsLine(
        SpannableStringBuilder s,
        String prefix,
        String stats
) {
    // IV / EV 以及 HP、Att、SpA 等文字保持默认黑色
    s.append(prefix);

    int statsStart = s.length();
    s.append(stats);

    // 只寻找数值
    java.util.regex.Pattern pattern =
            java.util.regex.Pattern.compile("\\d+");

    java.util.regex.Matcher matcher =
            pattern.matcher(stats);

    while (matcher.find()) {

        int start = statsStart + matcher.start();
        int end   = statsStart + matcher.end();

        // 数字变成纯蓝 #0000FF
        s.setSpan(
                new ForegroundColorSpan(0xFF0000FF),
                start,
                end,
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        );

        // 数字使用等宽字体，类似 CMD / Courier
        s.setSpan(
                new TypefaceSpan("monospace"),
                start,
                end,
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        );
    }
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

        LinearLayout header = new LinearLayout(this);
        header.setOrientation(LinearLayout.HORIZONTAL);
        header.setGravity(Gravity.CENTER_VERTICAL);

        TextView title = label("Gen III Save Inspector",24);
        title.setTypeface(null,1);
        header.addView(title, new LinearLayout.LayoutParams(0,-2,1f));

        filterButton = new Button(this);
        filterButton.setText("筛选");
        filterButton.setEnabled(false);
        filterButton.setOnClickListener(v -> showFilterDialog());
        header.addView(filterButton, new LinearLayout.LayoutParams(-2,-2));

        root.addView(header, new LinearLayout.LayoutParams(-1,-2));

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
        current=null;
        sourceStatus="";
        resetFilters();
        filterButton.setEnabled(false);
        list.removeAllViews();
        status.setText("正在读取…");
        try(InputStream in=getContentResolver().openInputStream(uri)) {
            if(in==null) throw new IOException("无法打开文件");
            byte[] data=readAll(in);
            current=Gen3SaveParser.parse(data);
            sourceStatus=current.info() + (data.length>Gen3SaveParser.SAVE_SIZE ? " · 已忽略尾部RTC/元数据" : "");
            filterButton.setEnabled(true);
            applyFilters();
        } catch(Exception e) {
            showError(e);
        }
    }

    private void resetFilters() {
        selectedNature=null;
        selectedSpeciesId=null;
        selectedIvFilter=IV_FILTER_NONE;
        if(filterButton!=null) filterButton.setText("筛选");
    }

    private int activeFilterCount() {
        int count=0;
        if(selectedNature!=null) count++;
        if(selectedSpeciesId!=null) count++;
        if(selectedIvFilter!=IV_FILTER_NONE) count++;
        return count;
    }

    private String primaryName(String name) {
        int slash=name.indexOf('/');
        return slash<0 ? name : name.substring(0,slash);
    }

    private void showFilterDialog() {
        if(current==null) return;

        LinearLayout content=new LinearLayout(this);
        content.setOrientation(LinearLayout.VERTICAL);
        content.setPadding(dp(24),dp(4),dp(24),0);

        content.addView(label("按性格",14),new LinearLayout.LayoutParams(-1,-2));
        List<String> natureValues=Arrays.asList(Gen3SaveParser.NATURES);
        List<String> natureLabels=new ArrayList<>();
        natureLabels.add("全部性格");
        for(String nature:natureValues) natureLabels.add(primaryName(nature));

        Spinner natureSpinner=new Spinner(this);
        ArrayAdapter<String> natureAdapter=new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item,natureLabels);
        natureAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        natureSpinner.setAdapter(natureAdapter);
        if(selectedNature!=null) {
            int index=natureValues.indexOf(selectedNature);
            if(index>=0) natureSpinner.setSelection(index+1);
        }
        content.addView(natureSpinner,new LinearLayout.LayoutParams(-1,-2));

        SortedMap<Integer,String> speciesNames=new TreeMap<>();
        Map<Integer,Integer> speciesCounts=new HashMap<>();
        for(Gen3SaveParser.Pokemon p:current.pokemon) {
            speciesNames.put(p.speciesId,p.species);
            speciesCounts.put(p.speciesId,speciesCounts.getOrDefault(p.speciesId,0)+1);
        }

        content.addView(label("按宝可梦种类",14),new LinearLayout.LayoutParams(-1,-2));
        List<Integer> speciesValues=new ArrayList<>(speciesNames.keySet());
        List<String> speciesLabels=new ArrayList<>();
        speciesLabels.add("全部种类（"+speciesValues.size()+"）");
        for(Integer speciesId:speciesValues) {
            speciesLabels.add(primaryName(speciesNames.get(speciesId))+
                    "（"+speciesCounts.get(speciesId)+"）");
        }

        Spinner speciesSpinner=new Spinner(this);
        ArrayAdapter<String> speciesAdapter=new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item,speciesLabels);
        speciesAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        speciesSpinner.setAdapter(speciesAdapter);
        if(selectedSpeciesId!=null) {
            int index=speciesValues.indexOf(selectedSpeciesId);
            if(index>=0) speciesSpinner.setSelection(index+1);
        }
        content.addView(speciesSpinner,new LinearLayout.LayoutParams(-1,-2));

        content.addView(label("按满个体值",14),new LinearLayout.LayoutParams(-1,-2));
        List<String> ivFilterLabels=Arrays.asList(
                "不限",
                "全部（任意一项 IV = 31）",
                "HP（HP = 31）",
                "Att（攻击 = 31）",
                "Def（防御 = 31）",
                "SpA（特攻 = 31）",
                "SpD（特防 = 31）",
                "Spe（速度 = 31）"
        );
        Spinner ivSpinner=new Spinner(this);
        ArrayAdapter<String> ivAdapter=new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item,ivFilterLabels);
        ivAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        ivSpinner.setAdapter(ivAdapter);
        ivSpinner.setSelection(selectedIvFilter);
        content.addView(ivSpinner,new LinearLayout.LayoutParams(-1,-2));

        new AlertDialog.Builder(this)
                .setTitle("筛选宝可梦")
                .setView(content)
                .setNegativeButton("取消",null)
                .setNeutralButton("清除",(dialog,which) -> {
                    resetFilters();
                    applyFilters();
                })
                .setPositiveButton("应用",(dialog,which) -> {
                    int naturePosition=natureSpinner.getSelectedItemPosition();
                    selectedNature=naturePosition==0 ? null : natureValues.get(naturePosition-1);

                    int speciesPosition=speciesSpinner.getSelectedItemPosition();
                    selectedSpeciesId=speciesPosition==0 ? null : speciesValues.get(speciesPosition-1);
                    selectedIvFilter=ivSpinner.getSelectedItemPosition();
                    applyFilters();
                })
                .show();
    }

    private boolean matchesIvFilter(Gen3SaveParser.Pokemon p) {
        switch(selectedIvFilter) {
            case IV_FILTER_ANY:
                return p.ivs.hp==31 || p.ivs.atk==31 || p.ivs.def==31 ||
                        p.ivs.spa==31 || p.ivs.spd==31 || p.ivs.spe==31;
            case IV_FILTER_HP:
                return p.ivs.hp==31;
            case IV_FILTER_ATK:
                return p.ivs.atk==31;
            case IV_FILTER_DEF:
                return p.ivs.def==31;
            case IV_FILTER_SPA:
                return p.ivs.spa==31;
            case IV_FILTER_SPD:
                return p.ivs.spd==31;
            case IV_FILTER_SPE:
                return p.ivs.spe==31;
            case IV_FILTER_NONE:
            default:
                return true;
        }
    }

    private void applyFilters() {
        if(current==null) return;
        List<Gen3SaveParser.Pokemon> filtered=new ArrayList<>();
        for(Gen3SaveParser.Pokemon p:current.pokemon) {
            if(selectedNature!=null && !selectedNature.equals(p.nature)) continue;
            if(selectedSpeciesId!=null && selectedSpeciesId.intValue()!=p.speciesId) continue;
            if(!matchesIvFilter(p)) continue;
            filtered.add(p);
        }

        int active=activeFilterCount();
        filterButton.setText(active==0 ? "筛选" : "筛选 ("+active+")");
        status.setText(sourceStatus+(active==0 ? "" : " · 筛选后 "+filtered.size()+" 只"));
        render(filtered);
    }

    private void render(List<Gen3SaveParser.Pokemon> mons) {
        list.removeAllViews();
        if(mons.isEmpty()) {
            String message=activeFilterCount()==0
                    ? "存档中没有可显示的宝可梦。"
                    : "没有符合当前筛选条件的宝可梦。\n请点击右上角“筛选”调整或清除条件。";
            TextView empty=label(message,14);
            empty.setTextColor(0xFF6B7280);
            list.addView(empty,new LinearLayout.LayoutParams(-1,-2));
            return;
        }
        for(int idx=0; idx<mons.size(); idx++) {
            Gen3SaveParser.Pokemon p=mons.get(idx);
            TextView row=label(String.format(Locale.US,"%03d  %s",idx+1,p.summary()),13);
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
