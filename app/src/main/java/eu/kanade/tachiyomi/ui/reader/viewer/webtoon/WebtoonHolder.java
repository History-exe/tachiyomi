package eu.kanade.tachiyomi.ui.reader.viewer.webtoon;

import android.support.v7.widget.RecyclerView;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;

import com.davemorrissey.labs.subscaleview.ImageSource;
import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView;

import butterknife.Bind;
import butterknife.ButterKnife;
import eu.kanade.tachiyomi.R;
import eu.kanade.tachiyomi.data.source.model.Page;
import eu.kanade.tachiyomi.ui.reader.ReaderActivity;

public class WebtoonHolder extends RecyclerView.ViewHolder {

    @Bind(R.id.page_image_view) SubsamplingScaleImageView imageView;
    @Bind(R.id.frame_container) ViewGroup container;
    @Bind(R.id.progress) ProgressBar progressBar;
    @Bind(R.id.retry_button) Button retryButton;

    private Page page;
    private WebtoonAdapter adapter;

    public WebtoonHolder(View view, WebtoonAdapter adapter, View.OnTouchListener touchListener) {
        super(view);
        this.adapter = adapter;
        ButterKnife.bind(this, view);

        int maxDim = ((ReaderActivity)adapter.getReader().getActivity()).getMaxBitmapSize();

        imageView.setParallelLoadingEnabled(true);
        imageView.setDoubleTapZoomStyle(SubsamplingScaleImageView.ZOOM_FOCUS_FIXED);
        imageView.setPanLimit(SubsamplingScaleImageView.PAN_LIMIT_INSIDE);
        imageView.setMinimumScaleType(SubsamplingScaleImageView.SCALE_TYPE_FIT_WIDTH);
        imageView.setMaxScale(10);
        imageView.setOnTouchListener(touchListener);
        imageView.setMaxDimensions(maxDim, maxDim);
        imageView.setOnImageEventListener(new SubsamplingScaleImageView.DefaultOnImageEventListener() {
            @Override
            public void onImageLoaded() {
                // When the image is loaded, reset the minimum height to avoid gaps
                container.setMinimumHeight(0);
            }
        });

        // Avoid to create a lot of view holders taking twice the screen height,
        // saving memory and a possible OOM. When the first image is loaded in this holder,
        // the minimum size will be removed.
        // Doing this we get sequential holder instantiation.
        container.setMinimumHeight(view.getResources().getDisplayMetrics().heightPixels * 2);

        // Leave some space between progress bars
        progressBar.setMinimumHeight(300);

        container.setOnTouchListener(touchListener);
        retryButton.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_UP) {
                if (page != null)
                    adapter.retryPage(page);
                return true;
            }
            return true;
        });
    }

    public void onSetValues(Page page) {
        this.page = page;
        switch (page.getStatus()) {
            case Page.QUEUE:
                onQueue();
                break;
            case Page.LOAD_PAGE:
                onLoading();
                break;
            case Page.DOWNLOAD_IMAGE:
                onLoading();
                break;
            case Page.READY:
                onReady();
                break;
            case Page.ERROR:
                onError();
                break;
        }
    }

    private void onLoading() {
        setErrorButtonVisible(false);
        setImageVisible(false);
        setProgressVisible(true);
    }

    private void onReady() {
        setErrorButtonVisible(false);
        setProgressVisible(false);
        setImageVisible(true);
        imageView.setRegionDecoderClass(adapter.getReader().getRegionDecoderClass());
        imageView.setImage(ImageSource.uri(page.getImagePath()));
    }

    private void onError() {
        setImageVisible(false);
        setProgressVisible(false);
        setErrorButtonVisible(true);
    }

    private void onQueue() {
        setImageVisible(false);
        setErrorButtonVisible(false);
        setProgressVisible(false);
    }

    private void setProgressVisible(boolean visible) {
        progressBar.setVisibility(visible ? View.VISIBLE : View.GONE);
    }

    private void setImageVisible(boolean visible) {
        imageView.setVisibility(visible ? View.VISIBLE : View.GONE);
    }

    private void setErrorButtonVisible(boolean visible) {
        retryButton.setVisibility(visible ? View.VISIBLE : View.GONE);
    }
}