package com.mobiletv.app;

import android.content.*;
import android.net.*;
import android.support.v7.widget.*;
import android.view.*;
import android.widget.*;
import java.util.*;
import com.mobiletv.image.*;

public class ChannelsAdapter extends RecyclerView.Adapter<ChannelsAdapter.MyViewHolder> {

    private Context mContext ;
    private List<Channels> mData ;


    public ChannelsAdapter(Context mContext, List<Channels> mData) {
        this.mContext = mContext;
        this.mData = mData;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View view ;
        LayoutInflater mInflater = LayoutInflater.from(mContext);
        view = mInflater.inflate(R.layout.cardview_item, parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, final int position) {

        holder.mTitle.setText(mData.get(position).getTitle());
		if (mData.get(position).getThumbnail() == null) {
			holder.mThumbnail.setImageResource(R.mipmap.ic_launcher);
		} else {
			holder.mThumbnail.setImageUrl(mData.get(position).getThumbnail());
		}


        holder.mCardView.setOnClickListener(new View.OnClickListener() {

				private Intent intent;
				@Override
				public void onClick(View v) {
					Intent intent = new Intent(mContext, PlayerActivity.class);

					intent.putExtra("title", mData.get(position).getTitle());
					intent.putExtra("description", mData.get(position).getDescription());
					intent.putExtra("thumbnail", mData.get(position).getThumbnail());
					intent.putExtra("url", mData.get(position).getUrl());
					intent.putExtra("key", mData.get(position).getKey());
					mContext.startActivity(intent);
				}
			});



    }

    @Override
    public int getItemCount() {
        return mData.size();
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder {

        TextView mTitle;
        SmartImageView mThumbnail;
        CardView mCardView ;

        public MyViewHolder(View itemView) {
            super(itemView);

            mTitle = itemView.findViewById(R.id.id_title) ;
            mThumbnail = itemView.findViewById(R.id.id_thumbnail);
            mCardView = itemView.findViewById(R.id.cardview_id);


        }
    }


}
