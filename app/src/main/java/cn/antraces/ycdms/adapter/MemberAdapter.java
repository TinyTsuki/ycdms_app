package cn.antraces.ycdms.adapter;

import static cn.antraces.ycdms.utils.Constants.PopPageDel;
import static cn.antraces.ycdms.utils.Constants.PopPageSelect;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import cn.antraces.ycdms.MyApplication;
import cn.antraces.ycdms.R;
import cn.antraces.ycdms.entity.Member;
import cn.antraces.ycdms.utils.C2RoundAngleImageView;
import cn.antraces.ycdms.utils.fns.Function;

@SuppressLint("NonConstantResourceId")
public class MemberAdapter extends RecyclerView.Adapter<MemberAdapter.ViewHolder> {
    private Context mContext;
    public List<Member> mMemberList;
    public Member selectedMember;
    private LocalBroadcastManager localBroadcastManager;
    private static View.OnLongClickListener onLongClickListener;

    static class ViewHolder extends RecyclerView.ViewHolder implements View.OnLongClickListener {
        @BindView(R.id.member_image)
        C2RoundAngleImageView memberImage;
        @BindView(R.id.member_name)
        TextView memberName;
        @BindView(R.id.member_class)
        TextView memberClass;
        @BindView(R.id.member_status)
        TextView memberStatus;
        @BindView(R.id.member)
        LinearLayout memberLayout;

        public ViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
        }

        @Override
        public boolean onLongClick(View view) {
            if (null != onLongClickListener)
                onLongClickListener.onLongClick(view);
            return false;
        }
    }

    public MemberAdapter(List<Member> MemberList) {
        mMemberList = MemberList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (mContext == null) mContext = parent.getContext();
        if (localBroadcastManager == null)
            localBroadcastManager = LocalBroadcastManager.getInstance(mContext);
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_main, parent, false);
        final ViewHolder holder = new ViewHolder(view);
        holder.memberLayout.setOnClickListener(view1 -> {
            int position = holder.getAdapterPosition();
            selectedMember = mMemberList.get(position);
            selectedMember.setPosition(position);

            Intent intent = new Intent(Function.getPackageName() + ".OPEN_POP_OPERATION_MEMBER_ACTIVITY");
            intent.putExtra("pop", PopPageSelect);
            localBroadcastManager.sendBroadcast(intent);
        });
        holder.memberLayout.setOnLongClickListener(view1 -> {
            int position = holder.getAdapterPosition();
            selectedMember = mMemberList.get(position);
            selectedMember.setPosition(position);

            Intent intent = new Intent(Function.getPackageName() + ".OPEN_POP_OPERATION_MEMBER_ACTIVITY");
            intent.putExtra("pop", PopPageDel);
            localBroadcastManager.sendBroadcast(intent);
            return true;
        });
        holder.memberImage.setOnClickListener(view1 -> {
            int position = holder.getAdapterPosition();
            Member member = mMemberList.get(position);
            if (Function.checkAppInstalled("com.tencent.mobileqq") || Function.checkAppInstalled("com.tencent.tim")) {
                mContext.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("mqqwpa://im/chat?chat_type=wpa&uin=" + member.getQq() + "&version=1")));
            } else {
                LocalBroadcastManager localBroadcastManager = LocalBroadcastManager.getInstance(MyApplication.getContext());
                Intent intent = new Intent(Function.getPackageName() + ".SHOW_SNACK_BAR_ACTIVITY");
                intent.putExtra("msg", mContext.getString(R.string.qq_not_installed));
                localBroadcastManager.sendBroadcast(intent);
            }
        });
        return holder;
    }

    @SuppressLint({"UseCompatLoadingForDrawables", "SetTextI18n"})
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Member member = mMemberList.get(position);

        holder.memberName.setText(member.getName() + " (" + member.getCn() + ")");
        holder.memberClass.setText(member.getMajor() + member.getClasse() + "班");
        if (member.getPhoto() > 0) {
            holder.memberStatus.setText(R.string.submitted);
            holder.memberStatus.setBackground(mContext.getResources().getDrawable(R.drawable.bg_green));
        } else {
            holder.memberStatus.setText(R.string.not_submitted);
            holder.memberStatus.setBackground(mContext.getResources().getDrawable(R.drawable.bg_red));
        }
        Glide.with(mContext).load(Function.getQQHeadUrl(member.getQq())).into(holder.memberImage);
    }

    @Override
    public int getItemCount() {
        return mMemberList.size();
    }

}
