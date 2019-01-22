package io.comrad.p2p;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.TextView;
import io.comrad.R;

import java.util.List;

public class PeerAdapter extends RecyclerView.Adapter<PeerAdapter.TextViewHolder> {
    private List<String> list;

    public static class TextViewHolder extends RecyclerView.ViewHolder {
        public TextView view;
        public TextViewHolder(TextView v) {
            super(v);
            view = v;
        }
    }

    public PeerAdapter(List<String> list) {
        this.list = list;
    }

    @Override
    public TextViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        TextView view = (TextView) LayoutInflater.from(parent.getContext())
                .inflate(R.layout.text, parent, false);

        return new TextViewHolder(view);
    }

    @Override
    public void onBindViewHolder(TextViewHolder holder, int position) {
        holder.view.setText(this.list.get(position));
    }

    @Override
    public int getItemCount() {
        return this.list.size();
    }

    public List<String> getList()
    {
        return this.list;
    }
}