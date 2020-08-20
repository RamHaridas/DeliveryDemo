package com.white.delivery.ui.offers;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.white.delivery.R;


public class OffersFragment extends Fragment {

    View view;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_offers, container, false);
        /*RecyclerView recyclerView = (RecyclerView) view.findViewById(R.id.recyclerView_offer);
        ListDataOffers[] myListData1 = new ListDataOffers[] {
                new ListDataOffers(null,"Test","test"),
                new ListDataOffers(null,"Test","test"),
                new ListDataOffers(null,"Test","test"),
                new ListDataOffers(null,"Test","test"),
                new ListDataOffers(null,"Test","test"),
                new ListDataOffers(null,"Test","test"),
                new ListDataOffers(null,"Test","test"),
                new ListDataOffers(null,"Test","test"),
                new ListDataOffers(null,"Test","test"),
        };
        ListAdapterOffers adapter = new ListAdapterOffers(myListData1);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);*/
        return view;
    }
}
