package net.osmand.plus.wikivoyage.search;

import android.graphics.drawable.LayerDrawable;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import net.osmand.plus.OsmandApplication;
import net.osmand.plus.R;
import net.osmand.plus.widgets.tools.CropCircleTransformation;
import net.osmand.plus.wikivoyage.data.WikivoyageArticle;
import net.osmand.plus.wikivoyage.data.WikivoyageSearchHistoryItem;
import net.osmand.plus.wikivoyage.data.WikivoyageSearchResult;
import net.osmand.util.Algorithms;

import java.util.ArrayList;
import java.util.List;

public class SearchRecyclerViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

	private static final int HEADER_TYPE = 0;
	private static final int ITEM_TYPE = 1;

	private OsmandApplication app;

	private LayerDrawable placeholder;
	private LayerDrawable historyPlaceholder;

	private List<Object> items = new ArrayList<>();

	private View.OnClickListener onItemClickListener;

	public void setOnItemClickListener(View.OnClickListener onItemClickListener) {
		this.onItemClickListener = onItemClickListener;
	}

	SearchRecyclerViewAdapter(OsmandApplication app) {
		this.app = app;
		placeholder = getPlaceholder(false);
		historyPlaceholder = getPlaceholder(true);
	}

	@NonNull
	@Override
	public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
		boolean header = viewType == HEADER_TYPE;
		int layoutId = header ? R.layout.wikivoyage_search_list_header : R.layout.wikivoyage_search_list_item;
		View itemView = LayoutInflater.from(viewGroup.getContext()).inflate(layoutId, viewGroup, false);
		if (header) {
			return new HeaderVH(itemView);
		}
		itemView.setOnClickListener(onItemClickListener);
		return new ItemVH(itemView);
	}

	@Override
	public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int pos) {
		if (viewHolder instanceof HeaderVH) {
			((HeaderVH) viewHolder).title.setText((String) getItem(pos));
		} else {
			ItemVH holder = (ItemVH) viewHolder;
			boolean lastItem = pos == getItemCount() - 1;

			Object item = getItem(pos);
			if (item instanceof WikivoyageSearchResult) {
				WikivoyageSearchResult searchRes = (WikivoyageSearchResult) item;
				Picasso.get()
						.load(app.getSettings().SHOW_IMAGES.get() ?
								WikivoyageArticle.getImageUrl(searchRes.getImageTitle(), true) : null)
						.transform(new CropCircleTransformation())
						.placeholder(placeholder)
						.into(holder.icon);
				holder.title.setText(searchRes.getArticleTitles().get(0));
				holder.leftDescr.setText(searchRes.getIsPartOf());
				holder.rightDescr.setText(searchRes.getFirstLangsString());
			} else {
				WikivoyageSearchHistoryItem historyItem = (WikivoyageSearchHistoryItem) item;
				holder.icon.setImageDrawable(historyPlaceholder);
				holder.title.setText(historyItem.getArticleTitle());
				holder.leftDescr.setText(historyItem.getIsPartOf());
				holder.rightDescr.setText(Algorithms.capitalizeFirstLetter(historyItem.getLang()));
			}
			holder.divider.setVisibility(lastItem ? View.GONE : View.VISIBLE);
			holder.shadow.setVisibility(lastItem ? View.VISIBLE : View.GONE);
		}
	}

	@Override
	public int getItemViewType(int position) {
		Object item = getItem(position);
		if (item instanceof String) {
			return HEADER_TYPE;
		}
		return ITEM_TYPE;
	}

	@Override
	public int getItemCount() {
		return items.size();
	}

	public Object getItem(int pos) {
		return items.get(pos);
	}

	public void setHistoryItems(@Nullable List<WikivoyageSearchHistoryItem> historyItems) {
		this.items.clear();
		if (historyItems != null && !historyItems.isEmpty()) {
			this.items.add(app.getString(R.string.shared_string_history));
			this.items.addAll(historyItems);
		}
		notifyDataSetChanged();
	}

	public void setItems(@Nullable List<WikivoyageSearchResult> items) {
		this.items.clear();
		if (items != null && !items.isEmpty()) {
			this.items.add(app.getString(R.string.shared_string_result));
			this.items.addAll(items);
		}
		notifyDataSetChanged();
	}

	private LayerDrawable getPlaceholder(boolean history) {
		LayerDrawable res = (LayerDrawable) ContextCompat.getDrawable(
				app, history
						? R.drawable.wikivoyage_search_history_placeholder
						: R.drawable.wikivoyage_search_placeholder
		);
		if (Build.VERSION.SDK_INT < 21 && res != null) {
			res.setDrawableByLayerId(R.id.placeholder_icon, app.getIconsCache().getIcon(
					history ? R.drawable.ic_action_history : R.drawable.ic_action_placeholder_city,
					R.color.icon_color
			));
		}
		return res;
	}

	static class HeaderVH extends RecyclerView.ViewHolder {

		final TextView title;

		HeaderVH(View itemView) {
			super(itemView);
			title = (TextView) itemView.findViewById(R.id.title);
		}
	}

	static class ItemVH extends RecyclerView.ViewHolder {

		final ImageView icon;
		final TextView title;
		final TextView leftDescr;
		final TextView rightDescr;
		final View divider;
		final View shadow;

		ItemVH(View itemView) {
			super(itemView);
			icon = (ImageView) itemView.findViewById(R.id.icon);
			title = (TextView) itemView.findViewById(R.id.title);
			leftDescr = (TextView) itemView.findViewById(R.id.left_description);
			rightDescr = (TextView) itemView.findViewById(R.id.right_description);
			divider = itemView.findViewById(R.id.divider);
			shadow = itemView.findViewById(R.id.shadow);
		}
	}
}
