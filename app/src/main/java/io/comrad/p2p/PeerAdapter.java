package io.comrad.p2p;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

import io.comrad.R;

public class PeerAdapter extends RecyclerView.Adapter<PeerAdapter.TextViewHolder> {
    private List<String> list;

    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
    public static class TextViewHolder extends RecyclerView.ViewHolder {
        // each data item is just a string in this case
        public TextView view;
        public TextViewHolder(TextView v) {
            super(v);
            view = v;
        }
    }

    // Provide a suitable constructor (depends on the kind of dataset)
    public PeerAdapter(List<String> list) {
        this.list = list;
    }

    // Create new views (invoked by the layout manager)
    @Override
    public TextViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // create a new vew
        TextView view = (TextView) LayoutInflater.from(parent.getContext())
                .inflate(R.layout.text, parent, false);

        return new TextViewHolder(view);
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(TextViewHolder holder, int position) {
        // - get element from your dataset at this position
        // - replace the contents of the view with that element
        holder.view.setText(this.list.get(position));

    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return this.list.size();
    }

    public List<String> getList()
    {
        return this.list;
    }
}