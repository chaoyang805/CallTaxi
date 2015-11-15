package com.chaoyang805.calltaxi.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import com.baidu.mapapi.search.sug.OnGetSuggestionResultListener;
import com.baidu.mapapi.search.sug.SuggestionResult;
import com.baidu.mapapi.search.sug.SuggestionSearch;
import com.baidu.mapapi.search.sug.SuggestionSearchOption;
import com.chaoyang805.calltaxi.R;
import com.chaoyang805.calltaxi.adapter.SuggestionInfoAdapter;
import com.chaoyang805.calltaxi.utils.ToastUtils;

import java.util.List;

/**
 * Created by chaoyang805 on 2015/11/8.
 */
public class DestSearchActivity extends AppCompatActivity implements View.OnClickListener, AdapterView.OnItemClickListener, OnGetSuggestionResultListener {

    private EditText mEtInputDest;

    private Button mBtnSearchDest;

    private ListView mDestList;

    private SuggestionInfoAdapter mAdapter;

    private SuggestionSearch mSuggestionSearch;

    private String mCity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dest_search);
        initViews();
        mSuggestionSearch = SuggestionSearch.newInstance();
        mSuggestionSearch.setOnGetSuggestionResultListener(this);
    }

    private void initViews() {
        mEtInputDest = (EditText) findViewById(R.id.et_input_dest);
        mBtnSearchDest = (Button) findViewById(R.id.btn_search_dest);
        mDestList = (ListView) findViewById(R.id.lv_dest);

        mEtInputDest.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                mSuggestionSearch.requestSuggestion(new SuggestionSearchOption()
                        .keyword(s.toString())
                        .city(getIntent().getStringExtra(MapActivity.EXTRA_CITY)));
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
        mBtnSearchDest.setOnClickListener(this);
        mDestList.setOnItemClickListener(this);
        mAdapter = new SuggestionInfoAdapter(this);
        mDestList.setAdapter(mAdapter);
    }


    @Override
    public void onClick(View v) {
        mSuggestionSearch.requestSuggestion(new SuggestionSearchOption()
                .keyword(mEtInputDest.getText().toString())
                .city(getIntent().getStringExtra(MapActivity.EXTRA_CITY)));
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        SuggestionResult.SuggestionInfo suggestionInfo = mAdapter.getItem(position);
        if (suggestionInfo != null) {
            Intent result = new Intent();
            result.putExtra(MapActivity.EXTRA_DEST, suggestionInfo.key);
            result.putExtra(MapActivity.EXTRA_LATITUDE, suggestionInfo.pt.latitude);
            result.putExtra(MapActivity.EXTRA_LONGITUDE, suggestionInfo.pt.longitude);
            setResult(Activity.RESULT_OK, result);
            finish();
        }else {
            ToastUtils.showToast(this,"搜索出错了，选别的试试吧");
        }
    }

    @Override
    public void onGetSuggestionResult(SuggestionResult result) {

        if (result == null || result.getAllSuggestions() == null) {
            return;
        }
        List<SuggestionResult.SuggestionInfo> suggestions = result.getAllSuggestions();
        mAdapter.addAll(suggestions);
    }
}
