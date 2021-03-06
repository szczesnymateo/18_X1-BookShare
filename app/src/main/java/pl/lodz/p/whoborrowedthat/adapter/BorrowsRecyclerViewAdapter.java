package pl.lodz.p.whoborrowedthat.adapter;

import android.annotation.SuppressLint;
import android.app.Application;
import android.content.Intent;
import android.graphics.Paint;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import pl.lodz.p.whoborrowedthat.R;
import pl.lodz.p.whoborrowedthat.controller.BorrowedStuffDetailActivity;
import pl.lodz.p.whoborrowedthat.helper.SharedPrefHelper;
import pl.lodz.p.whoborrowedthat.model.Stuff;
import pl.lodz.p.whoborrowedthat.model.User;
import pl.lodz.p.whoborrowedthat.service.ApiManager;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import java.text.SimpleDateFormat;
import java.time.Duration;
import java.util.Date;
import java.util.List;

import static pl.lodz.p.whoborrowedthat.helper.ConstHelper.STUFF_BUNDLE__KEY;

public class BorrowsRecyclerViewAdapter extends RecyclerView.Adapter<BorrowsRecyclerViewAdapter.ViewHolder> {

    private final LayoutInflater inflater;
    private List<Stuff> stuffs;
    private Application application;

    public BorrowsRecyclerViewAdapter(Application application) {
        this.application = application;
        inflater = LayoutInflater.from(application.getApplicationContext());

    }

    @NonNull
    @Override
    public BorrowsRecyclerViewAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = inflater.inflate(R.layout.stuff_item, parent, false);
        return new ViewHolder(itemView);
    }

    @SuppressLint({"RecyclerView", "SetTextI18n"})
    @Override
    public void onBindViewHolder(@NonNull BorrowsRecyclerViewAdapter.ViewHolder holder, final int position) {
        if (stuffs != null) {
            final Stuff current = stuffs.get(position);

            if (current.getReturnDate() == null) {
                holder.content.setText(current.getName());
                Date now = new Date();
                Date returnDate = current.getEstimatedReturnDate();
                Date diff = new Date(returnDate.getTime() - now.getTime());
                holder.daysLeft.setText(String.valueOf(diff.getTime() / (24 * 3600000)));
            } else {
                holder.daysLeft.setText("");
                holder.daysLeftTitle.setText("");
                holder.content.setText(current.getName());
                holder.content.setPaintFlags(holder.content.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
            }



            if (current.getNotified()) {
                holder.notification.setVisibility(View.VISIBLE);
            } else {
                holder.notification.setVisibility(View.INVISIBLE);
            }

            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    User user = SharedPrefHelper.getUserFormSP(application);

                    ApiManager.getInstance().readNotification(user, stuffs.get(position).getId(), new Callback<Object>() {
                        @Override
                        public void onResponse(@NonNull Call<Object> call, @NonNull Response<Object> response) {
                            Log.d("RESPONSE", "Remainder sent!");
                        }

                        @Override
                        public void onFailure(@NonNull Call<Object> call, @NonNull Throwable t) {
                            Log.d("RESPONSE", "Cannot send a remainder :(");
                        }
                    });

                    Intent intent = new Intent(v.getContext(), BorrowedStuffDetailActivity.class);
                    Bundle bundle = new Bundle();
                    bundle.putSerializable(STUFF_BUNDLE__KEY, stuffs.get(position));
                    intent.putExtras(bundle);
                    v.getContext().startActivity(intent);
                }
            });
        } else {
            holder.content.setText("Name not found...");
        }
    }

    public void setStuffs(List<Stuff> stuffs){
        this.stuffs = stuffs;
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        if (stuffs != null)
            return stuffs.size();
        else return 0;
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView content;
        private final ImageView notification;
        private final TextView daysLeft;
        private final TextView daysLeftTitle;

        private ViewHolder(View itemView) {
            super(itemView);
            content = itemView.findViewById(R.id.content);
            notification = itemView.findViewById(R.id.notification);
            daysLeft = itemView.findViewById(R.id.numberOfDaysLeft);
            daysLeftTitle = itemView.findViewById(R.id.daysLeftTitle);
        }
    }
}
