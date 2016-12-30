package valet.digikom.com.valetparking.fragments;


import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import valet.digikom.com.valetparking.R;
import valet.digikom.com.valetparking.adapter.ListDefectAdapter;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link StepTwoFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class StepTwoFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    public static final String ARG_POSITION = "pos";
    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
    ListView mListviewDefect;
    List<String> listDefects;
    ListDefectAdapter adapter;
    private OnDefectSelectedListener defectListener;
    ArrayList<Integer> selecedPosition = new ArrayList<>();

    public StepTwoFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment StepTwoFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static StepTwoFragment newInstance(String param1, String param2) {
        StepTwoFragment fragment = new StepTwoFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        listDefects = Arrays.asList(getContext().getResources().getStringArray(R.array.car_defects));
        adapter = new ListDefectAdapter(getContext(), listDefects);
        if (savedInstanceState != null){
            //selecedPosition = savedInstanceState.getIntegerArrayList(ARG_POSITION);
            //adapter.setSelectedPosition(selecedPosition);
        }
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view =  inflater.inflate(R.layout.fragment_step_two, container, false);
        mListviewDefect = (ListView) view.findViewById(R.id.listview_defects);
        mListviewDefect.setAdapter(adapter);
        mListviewDefect.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                CheckBox cb = (CheckBox) view.findViewById(R.id.checkbox_defect);
                String defect = adapter.getItem(i);
                if (cb.isChecked()) {
                    cb.setChecked(false);
                    defectListener.onDefectUnselected(defect);
                    //selecedPosition.remove(i);
                }else {
                    cb.setChecked(true);
                    defectListener.onDefectSelected(defect);
                    //selecedPosition.add(i);
                }
                //Toast.makeText(getContext(),"Item clicked", Toast.LENGTH_SHORT).show();
            }
        });

        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        defectListener = (OnDefectSelectedListener) context;
    }

    public interface OnDefectSelectedListener{
        void onDefectSelected(String defect);
        void onDefectUnselected(String defect);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        //outState.putIntegerArrayList(ARG_POSITION, selecedPosition);
        super.onSaveInstanceState(outState);
    }
}
