package com.example.gen3saveinspector;

import android.app.*;
import android.content.*;
import android.database.Cursor;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.provider.DocumentsContract;
import android.provider.OpenableColumns;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.text.style.TypefaceSpan;
import android.view.Gravity;
import android.view.View;
import android.widget.*;

import java.io.*;
import java.util.*;

public class MainActivity extends Activity {
    private static final int REQ_OPEN = 1001;
    private static final int REQ_CREATE_BACKUP = 1002;
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
    private Button cancelDeleteButton;
    private Button deleteButton;
    private Button openButton;
    private Gen3SaveParser.Result current;
    private Uri sourceUri;
    private byte[] sourceBytes;
    private String sourceDisplayName;
    private boolean sourceWriteGranted;
    private String sourceStatus = "";
    private String selectedNature;
    private Integer selectedSpeciesId;
    private int selectedIvFilter = IV_FILTER_NONE;
    private boolean deleteMode;
    private final Set<Gen3SaveParser.Pokemon> selectedForDeletion =
            Collections.newSetFromMap(new IdentityHashMap<>());
    private final Map<Gen3SaveParser.Pokemon,CheckBox> deletionCheckboxes =
            new IdentityHashMap<>();
    private byte[] pendingOriginalBytes;
    private byte[] pendingEditedBytes;
    private int pendingDeleteCount;
    private boolean transactionBusy;

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

        cancelDeleteButton = new Button(this);
        cancelDeleteButton.setText("取消");
        cancelDeleteButton.setVisibility(View.GONE);
        cancelDeleteButton.setOnClickListener(v -> exitDeleteMode());
        header.addView(cancelDeleteButton,new LinearLayout.LayoutParams(-2,-2));

        deleteButton = new Button(this);
        deleteButton.setText("删除");
        deleteButton.setVisibility(View.GONE);
        deleteButton.setOnClickListener(v -> confirmDeletion());
        header.addView(deleteButton,new LinearLayout.LayoutParams(-2,-2));

        root.addView(header, new LinearLayout.LayoutParams(-1,-2));

        TextView sub = label("读取第三世代 .sav/.srm：查看详情与筛选；长按宝可梦可备份后删除。",14);
        sub.setTextColor(0xFF4B5563);
        root.addView(sub);

        openButton = new Button(this);
        openButton.setText("选择 .sav / .srm 存档");
        openButton.setOnClickListener(v -> chooseFile());
        root.addView(openButton, new LinearLayout.LayoutParams(-1,-2));

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
        i.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION |
                Intent.FLAG_GRANT_WRITE_URI_PERMISSION |
                Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
        startActivityForResult(i,REQ_OPEN);
    }

    @Override protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode,resultCode,data);
        if(requestCode==REQ_OPEN && resultCode==RESULT_OK && data!=null && data.getData()!=null) {
            Uri uri=data.getData();
            try {
                final int flags = data.getFlags() & (Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                if(flags!=0) {
                    try { getContentResolver().takePersistableUriPermission(uri,flags); }
                    catch(Exception ignored) {
                        try {
                            if((flags & Intent.FLAG_GRANT_READ_URI_PERMISSION)!=0)
                                getContentResolver().takePersistableUriPermission(uri,
                                        Intent.FLAG_GRANT_READ_URI_PERMISSION);
                        } catch(Exception ignoredRead) { /* 当前会话的临时授权仍然有效。 */ }
                    }
                }
                loadUri(uri,(flags & Intent.FLAG_GRANT_WRITE_URI_PERMISSION)!=0);
            } catch(Exception e) {
                showError(e);
            }
            return;
        }
        if(requestCode==REQ_CREATE_BACKUP) {
            if(resultCode!=RESULT_OK || data==null || data.getData()==null) {
                cancelPendingDeletion("已取消，原存档未修改。");
            } else {
                finishDeletionAfterBackup(data.getData());
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

    private byte[] readUri(Uri uri) throws IOException {
        try(InputStream in=getContentResolver().openInputStream(uri)) {
            if(in==null) throw new IOException("无法打开文件");
            return readAll(in);
        }
    }

    private String displayName(Uri uri) {
        try(Cursor cursor=getContentResolver().query(uri,
                new String[]{OpenableColumns.DISPLAY_NAME},null,null,null)) {
            if(cursor!=null && cursor.moveToFirst()) {
                int column=cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                if(column>=0) {
                    String name=cursor.getString(column);
                    if(name!=null && !name.trim().isEmpty()) return name;
                }
            }
        } catch(Exception ignored) {}
        String last=uri.getLastPathSegment();
        return last==null || last.trim().isEmpty() ? "save.sav" : last;
    }

    private void loadUri(Uri uri, boolean writeGranted) {
        current=null;
        sourceUri=null;
        sourceBytes=null;
        sourceDisplayName=null;
        sourceWriteGranted=false;
        sourceStatus="";
        leaveDeleteModeWithoutRendering();
        clearPendingTransaction();
        resetFilters();
        filterButton.setEnabled(false);
        list.removeAllViews();
        status.setText("正在读取…");
        try {
            byte[] data=readUri(uri);
            current=Gen3SaveParser.parse(data);
            sourceUri=uri;
            sourceBytes=data;
            sourceDisplayName=displayName(uri);
            sourceWriteGranted=writeGranted ||
                    checkCallingOrSelfUriPermission(uri,Intent.FLAG_GRANT_WRITE_URI_PERMISSION)==
                            android.content.pm.PackageManager.PERMISSION_GRANTED;
            sourceStatus=current.info() + (data.length>Gen3SaveParser.SAVE_SIZE ? " · 含尾部 RTC/元数据" : "");
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

    private boolean documentIsExplicitlyReadOnly() {
        if(sourceUri==null || !DocumentsContract.isDocumentUri(this,sourceUri)) return false;
        try(Cursor cursor=getContentResolver().query(sourceUri,
                new String[]{DocumentsContract.Document.COLUMN_FLAGS},null,null,null)) {
            if(cursor!=null && cursor.moveToFirst()) {
                int column=cursor.getColumnIndex(DocumentsContract.Document.COLUMN_FLAGS);
                if(column>=0) {
                    int flags=cursor.getInt(column);
                    return (flags & DocumentsContract.Document.FLAG_SUPPORTS_WRITE)==0;
                }
            }
        } catch(Exception ignored) {}
        return false;
    }

    private boolean canStartDeletion() {
        if(sourceUri==null || sourceBytes==null || current==null) return false;
        boolean hasWriteGrant=sourceWriteGranted ||
                checkCallingOrSelfUriPermission(sourceUri,Intent.FLAG_GRANT_WRITE_URI_PERMISSION)==
                        android.content.pm.PackageManager.PERMISSION_GRANTED;
        if(hasWriteGrant && !documentIsExplicitlyReadOnly()) return true;
        new AlertDialog.Builder(this)
                .setTitle("此存档只能读取")
                .setMessage("当前文件或文档提供方没有授予写入权限。请通过上方按钮重新选择一个可写的 .sav/.srm 文件。")
                .setPositiveButton("确定",null)
                .show();
        return false;
    }

    private void enterDeleteMode(Gen3SaveParser.Pokemon pokemon) {
        if(deleteMode || transactionBusy || !canStartDeletion()) return;
        deleteMode=true;
        for(CheckBox box:deletionCheckboxes.values()) box.setVisibility(View.VISIBLE);
        CheckBox selected=deletionCheckboxes.get(pokemon);
        if(selected!=null) selected.setChecked(true);
        else selectedForDeletion.add(pokemon);
        updateDeleteActions();
    }

    private void toggleDeletionSelection(Gen3SaveParser.Pokemon pokemon, CheckBox box) {
        if(!deleteMode || transactionBusy) return;
        box.setChecked(!box.isChecked());
    }

    private void updateDeleteActions() {
        if(filterButton==null || cancelDeleteButton==null || deleteButton==null) return;
        filterButton.setVisibility(deleteMode ? View.GONE : View.VISIBLE);
        cancelDeleteButton.setVisibility(deleteMode ? View.VISIBLE : View.GONE);
        deleteButton.setVisibility(deleteMode ? View.VISIBLE : View.GONE);
        int count=selectedForDeletion.size();
        deleteButton.setText(count==0 ? "删除" : "删除 ("+count+")");
        deleteButton.setEnabled(deleteMode && count>0 && !transactionBusy);
        cancelDeleteButton.setEnabled(!transactionBusy);
    }

    private void leaveDeleteModeWithoutRendering() {
        deleteMode=false;
        selectedForDeletion.clear();
        for(CheckBox box:deletionCheckboxes.values()) {
            box.setChecked(false);
            box.setVisibility(View.GONE);
        }
        updateDeleteActions();
    }

    private void exitDeleteMode() {
        if(transactionBusy) return;
        leaveDeleteModeWithoutRendering();
    }

    @Override public void onBackPressed() {
        if(deleteMode) exitDeleteMode();
        else super.onBackPressed();
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
        deletionCheckboxes.clear();
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
            LinearLayout item=new LinearLayout(this);
            item.setOrientation(LinearLayout.HORIZONTAL);
            item.setGravity(Gravity.CENTER_VERTICAL);
            item.setBackgroundColor(0xFFFFFFFF);

            TextView row=label(String.format(Locale.US,"%03d  %s",idx+1,p.summary()),13);
            row.setText(formatPokemonSummary(p));
            row.setPadding(dp(12),dp(10),dp(12),dp(10));

            CheckBox checkbox=new CheckBox(this);
            checkbox.setContentDescription("选择 "+p.species+"，"+p.location);
            checkbox.setChecked(selectedForDeletion.contains(p));
            checkbox.setVisibility(deleteMode ? View.VISIBLE : View.GONE);
            checkbox.setOnCheckedChangeListener((buttonView,isChecked) -> {
                if(!deleteMode) return;
                if(isChecked) selectedForDeletion.add(p);
                else selectedForDeletion.remove(p);
                updateDeleteActions();
            });
            deletionCheckboxes.put(p,checkbox);

            View.OnClickListener click=v -> {
                if(deleteMode) toggleDeletionSelection(p,checkbox);
                else new AlertDialog.Builder(this)
                        .setTitle(p.species)
                        .setMessage(p.detail())
                        .setPositiveButton("关闭",null)
                        .show();
            };
            View.OnLongClickListener longClick=v -> {
                if(!deleteMode) enterDeleteMode(p);
                else if(!checkbox.isChecked()) checkbox.setChecked(true);
                return true;
            };
            row.setOnClickListener(click);
            row.setOnLongClickListener(longClick);
            item.setOnClickListener(click);
            item.setOnLongClickListener(longClick);

            item.addView(row,new LinearLayout.LayoutParams(0,-2,1f));
            item.addView(checkbox,new LinearLayout.LayoutParams(-2,-2));
            LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,-2);
            lp.setMargins(0,0,0,dp(6));
            list.addView(item,lp);
        }
        updateDeleteActions();
    }

    private String backupName(String originalName) {
        String name=originalName==null || originalName.trim().isEmpty() ? "save.sav" : originalName;
        int dot=name.lastIndexOf('.');
        if(dot>0) return name.substring(0,dot)+"_backup"+name.substring(dot);
        return name+"_backup";
    }

    private void setTransactionBusy(boolean busy) {
        transactionBusy=busy;
        if(openButton!=null) openButton.setEnabled(!busy);
        if(filterButton!=null) filterButton.setEnabled(!busy && current!=null);
        updateDeleteActions();
    }

    private void clearPendingTransaction() {
        pendingOriginalBytes=null;
        pendingEditedBytes=null;
        pendingDeleteCount=0;
        setTransactionBusy(false);
    }

    private void cancelPendingDeletion(String message) {
        clearPendingTransaction();
        if(message!=null) Toast.makeText(this,message,Toast.LENGTH_LONG).show();
    }

    private void confirmDeletion() {
        if(!deleteMode || selectedForDeletion.isEmpty() || transactionBusy) return;
        int count=selectedForDeletion.size();
        String suggested=backupName(sourceDisplayName);
        new AlertDialog.Builder(this)
                .setTitle("确认删除 "+count+" 项？")
                .setMessage("下一步会请你保存备份，建议文件名为“"+suggested+"”。只有备份写入并校验成功后，才会修改原存档。")
                .setNegativeButton("取消",null)
                .setPositiveButton("继续并备份",(dialog,which) -> prepareDeletion())
                .show();
    }

    private void prepareDeletion() {
        if(transactionBusy || !canStartDeletion()) return;
        setTransactionBusy(true);
        try {
            pendingOriginalBytes=Arrays.copyOf(sourceBytes,sourceBytes.length);
            pendingEditedBytes=Gen3SaveParser.deletePokemon(sourceBytes,
                    new ArrayList<>(selectedForDeletion));
            // 写文件之前再完整解析一次内存副本。
            Gen3SaveParser.parse(pendingEditedBytes);
            pendingDeleteCount=selectedForDeletion.size();

            Intent intent=new Intent(Intent.ACTION_CREATE_DOCUMENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            String type=getContentResolver().getType(sourceUri);
            intent.setType(type==null || type.trim().isEmpty() ? "application/octet-stream" : type);
            intent.putExtra(Intent.EXTRA_TITLE,backupName(sourceDisplayName));
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION |
                    Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
            startActivityForResult(intent,REQ_CREATE_BACKUP);
        } catch(Exception e) {
            clearPendingTransaction();
            showDeleteError("无法准备删除",e.getMessage()==null ? e.toString() : e.getMessage());
        }
    }

    private void writeUri(Uri uri, byte[] bytes) throws IOException {
        try(OutputStream out=getContentResolver().openOutputStream(uri,"rwt")) {
            if(out==null) throw new IOException("无法打开目标文件进行写入。");
            out.write(bytes);
            out.flush();
        }
    }

    private void finishDeletionAfterBackup(Uri backupUri) {
        if(pendingOriginalBytes==null || pendingEditedBytes==null || sourceUri==null) {
            cancelPendingDeletion(null);
            showDeleteError("删除已安全中止","待处理的数据已经失效，原存档未由本程序修改。请重新选择存档后再试。");
            return;
        }
        if(sourceUri.equals(backupUri)) {
            cancelPendingDeletion(null);
            showDeleteError("备份位置无效","备份不能与原存档是同一个文件。原存档未由本程序修改。");
            return;
        }

        setTransactionBusy(true);
        byte[] original=pendingOriginalBytes;
        byte[] edited=pendingEditedBytes;
        int deleteCount=pendingDeleteCount;
        String actualBackupName=displayName(backupUri);

        try {
            writeUri(backupUri,original);
            byte[] backupCheck=readUri(backupUri);
            if(!Arrays.equals(original,backupCheck))
                throw new IOException("备份写入后的内容与原存档不一致。");
        } catch(Exception e) {
            clearPendingTransaction();
            showDeleteError("备份失败，原存档未修改",
                    (e.getMessage()==null ? e.toString() : e.getMessage())+
                            "\n可能留下了不完整的备份文件，请将其删除后重试。");
            return;
        }

        byte[] liveSource;
        try {
            liveSource=readUri(sourceUri);
            if(!Arrays.equals(original,liveSource)) {
                clearPendingTransaction();
                leaveDeleteModeWithoutRendering();
                try {
                    current=Gen3SaveParser.parse(liveSource);
                    sourceBytes=liveSource;
                    sourceStatus=current.info()+
                            (liveSource.length>Gen3SaveParser.SAVE_SIZE ? " · 含尾部 RTC/元数据" : "");
                    applyFilters();
                } catch(Exception ignored) {
                    invalidateCurrentSource();
                }
                showDeleteError("原存档已发生变化",
                        "创建备份期间，原存档被其他程序修改，因此没有覆盖它。已验证的备份为“"+
                                actualBackupName+"”。请重新载入最新存档后再试。");
                return;
            }
        } catch(Exception e) {
            clearPendingTransaction();
            showDeleteError("无法重新读取原存档",
                    "已验证的备份“"+actualBackupName+"”已保存，但原存档尚未修改。\n"+
                            (e.getMessage()==null ? e.toString() : e.getMessage()));
            return;
        }

        try {
            writeUri(sourceUri,edited);
            byte[] written=readUri(sourceUri);
            if(!Arrays.equals(edited,written))
                throw new IOException("原存档写入后的内容校验不一致。");
            Gen3SaveParser.Result parsed=Gen3SaveParser.parse(written);

            current=parsed;
            sourceBytes=written;
            sourceStatus=current.info()+
                    (written.length>Gen3SaveParser.SAVE_SIZE ? " · 含尾部 RTC/元数据" : "")+
                    " · 已删除 "+deleteCount+" 项 · 备份："+actualBackupName;
            clearPendingTransaction();
            leaveDeleteModeWithoutRendering();
            clearUnavailableSpeciesFilter();
            filterButton.setEnabled(true);
            applyFilters();
            Toast.makeText(this,"删除完成，备份已验证："+actualBackupName,Toast.LENGTH_LONG).show();
        } catch(Exception writeError) {
            boolean restored=false;
            String restoreProblem=null;
            try {
                writeUri(sourceUri,original);
                byte[] restoredBytes=readUri(sourceUri);
                if(!Arrays.equals(original,restoredBytes))
                    throw new IOException("恢复后的内容校验不一致。");
                current=Gen3SaveParser.parse(restoredBytes);
                sourceBytes=restoredBytes;
                sourceStatus=current.info()+
                        (restoredBytes.length>Gen3SaveParser.SAVE_SIZE ? " · 含尾部 RTC/元数据" : "")+
                        " · 删除失败，已恢复原存档 · 备份："+actualBackupName;
                restored=true;
            } catch(Exception restoreError) {
                restoreProblem=restoreError.getMessage()==null ? restoreError.toString() : restoreError.getMessage();
            }

            clearPendingTransaction();
            leaveDeleteModeWithoutRendering();
            if(restored) {
                applyFilters();
                showDeleteError("删除失败，已恢复原存档",
                        (writeError.getMessage()==null ? writeError.toString() : writeError.getMessage())+
                                "\n备份“"+actualBackupName+"”仍然保留。原存档已自动恢复并通过校验。");
            } else {
                invalidateCurrentSource();
                showDeleteError("严重错误：请使用备份恢复",
                        "写入原存档失败，自动恢复也未通过校验。请不要继续使用当前原文件，改用已验证的备份“"+
                                actualBackupName+"”。\n恢复错误："+restoreProblem);
            }
        }
    }

    private void clearUnavailableSpeciesFilter() {
        if(selectedSpeciesId==null || current==null) return;
        for(Gen3SaveParser.Pokemon pokemon:current.pokemon)
            if(pokemon.speciesId==selectedSpeciesId.intValue()) return;
        selectedSpeciesId=null;
    }

    private void invalidateCurrentSource() {
        current=null;
        sourceUri=null;
        sourceBytes=null;
        sourceDisplayName=null;
        sourceWriteGranted=false;
        filterButton.setEnabled(false);
        list.removeAllViews();
        status.setText("原存档状态未知，请从已验证的备份恢复");
    }

    private void showDeleteError(String title, String message) {
        new AlertDialog.Builder(this)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton("确定",null)
                .show();
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
