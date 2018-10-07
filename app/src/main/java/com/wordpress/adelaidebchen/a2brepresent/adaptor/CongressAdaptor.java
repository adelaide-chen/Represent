package com.wordpress.adelaidebchen.a2brepresent.adaptor;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.wordpress.adelaidebchen.a2brepresent.MainActivity;
import com.wordpress.adelaidebchen.a2brepresent.R;
import com.wordpress.adelaidebchen.a2brepresent.model.Person;

import java.util.List;

public class CongressAdaptor extends RecyclerView.Adapter<CongressAdaptor.CongressAdaptorViewHolder> {
    final private List<Person> people;
    final private Context context;

    public CongressAdaptor(List<Person> people, Context context) {
        this.people = people;
        this.context = context;
    }

    @Override
    public CongressAdaptorViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View personView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.congress_card, parent, false);
        return new CongressAdaptorViewHolder(personView);
    }

    @Override
    public void onBindViewHolder(CongressAdaptorViewHolder holder, final int i) {
        holder.txtname.setText(people.get(i).getPersonName());
        ((MainActivity)context).setPartyIcon(people.get(i), holder.imgparty);
        holder.imginfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((MainActivity)context).detailedView(people.get(i));
            }
        });
    }

    @Override
    public int getItemCount() {
        return people.size();
    }

    public class CongressAdaptorViewHolder extends RecyclerView.ViewHolder {
        final TextView txtname;
        final ImageView imgparty;
        final ImageView imginfo;

        CongressAdaptorViewHolder(View view) {
            super(view);
            txtname = view.findViewById(R.id.personName);
            imgparty = view.findViewById(R.id.affiliation);
            imginfo = view.findViewById(R.id.moreInformation);
        }
    }
}

