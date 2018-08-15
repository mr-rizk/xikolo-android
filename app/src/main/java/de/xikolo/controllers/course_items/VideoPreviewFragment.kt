package de.xikolo.controllers.course_items

import android.content.res.Configuration
import android.graphics.Point
import android.os.Bundle
import android.view.*
import android.widget.LinearLayout
import android.widget.TextView
import butterknife.BindView
import com.yatatsu.autobundle.AutoBundleField
import de.xikolo.R
import de.xikolo.config.GlideApp
import de.xikolo.controllers.base.LoadingStatePresenterFragment
import de.xikolo.controllers.helper.DownloadViewHelper
import de.xikolo.controllers.video.VideoActivityAutoBundle
import de.xikolo.models.*
import de.xikolo.presenters.base.PresenterFactory
import de.xikolo.presenters.course_items.VideoPreviewPresenter
import de.xikolo.presenters.course_items.VideoPreviewPresenterFactory
import de.xikolo.presenters.course_items.VideoPreviewView
import de.xikolo.utils.CastUtil
import de.xikolo.views.CustomSizeImageView
import java.util.concurrent.TimeUnit

class VideoPreviewFragment : LoadingStatePresenterFragment<VideoPreviewPresenter, VideoPreviewView>(), VideoPreviewView {

    companion object {
        val TAG: String = VideoPreviewFragment::class.java.simpleName
    }

    @AutoBundleField()
    lateinit var courseId: String

    @AutoBundleField
    lateinit var sectionId: String

    @AutoBundleField
    lateinit var itemId: String

    @BindView(R.id.textTitle)
    lateinit var textTitle: TextView

    @BindView(R.id.durationText)
    lateinit var textDuration: TextView

    @BindView(R.id.videoThumbnail)
    lateinit var imageVideoThumbnail: CustomSizeImageView

    @BindView(R.id.containerDownloads)
    lateinit var linearLayoutDownloads: LinearLayout

    @BindView(R.id.refresh_layout)
    lateinit var viewContainer: View

    @BindView(R.id.playButton)
    lateinit var viewPlay: View

    @BindView(R.id.videoMetadata)
    lateinit var videoMetadata: ViewGroup

    private lateinit var hdVideo: DownloadViewHelper
    private lateinit var sdVideo: DownloadViewHelper
    private lateinit var slides: DownloadViewHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun getLayoutResource(): Int {
        return R.layout.content_video
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        this.viewContainer.visibility = View.GONE

        if (activity?.resources?.configuration?.orientation == Configuration.ORIENTATION_PORTRAIT) {
            val display = activity!!.windowManager.defaultDisplay
            val size = Point()
            display.getSize(size)
            imageVideoThumbnail.setDimensions(size.x, size.x / 16 * 9)
        } else {
            val display = activity!!.windowManager.defaultDisplay
            val size = Point()
            display.getSize(size)
            imageVideoThumbnail.setDimensions(
                (size.x * 0.6).toInt(),
                (size.x * 0.6 / 16 * 9).toInt()
            )

            val paramsMeta = videoMetadata.layoutParams
            paramsMeta.width = (size.x * 0.6).toInt()
            videoMetadata.layoutParams = paramsMeta
        }
    }

    override fun setupView(course: Course, section: Section, item: Item, video: Video) {
        hideProgress()
        viewContainer.visibility = View.VISIBLE

        GlideApp.with(this)
            .load(video.thumbnailUrl)
            .override(imageVideoThumbnail.forcedWidth, imageVideoThumbnail.forcedHeight)
            .into(imageVideoThumbnail)

        textTitle.text = video.title

        linearLayoutDownloads.removeAllViews()

        activity?.let { activity ->
            hdVideo = DownloadViewHelper(
                activity,
                DownloadAsset.Course.Item.VideoHD(item, video),
                activity.getText(R.string.video_hd_as_mp4)
            )
            hdVideo.openFileAsVideo { presenter.onPlayClicked() }
            linearLayoutDownloads.addView(hdVideo.view)

            sdVideo = DownloadViewHelper(
                activity,
                DownloadAsset.Course.Item.VideoSD(item, video),
                activity.getText(R.string.video_sd_as_mp4)
            )
            sdVideo.openFileAsVideo { presenter.onPlayClicked() }
            linearLayoutDownloads.addView(sdVideo.view)

            slides = DownloadViewHelper(
                activity,
                DownloadAsset.Course.Item.Slides(item, video),
                activity.getText(R.string.slides_as_pdf)
            )
            slides.openFileAsPdf()
            linearLayoutDownloads.addView(slides.view)
        }

        val minutes = TimeUnit.SECONDS.toMinutes(video.duration.toLong())
        val seconds = video.duration - TimeUnit.MINUTES.toSeconds(TimeUnit.SECONDS.toMinutes(video.duration.toLong()))
        textDuration.text = getString(R.string.duration, minutes, seconds)

        viewPlay.setOnClickListener { _ -> presenter.onPlayClicked() }
    }

    override fun startVideo(video: Video) {
        activity?.let {
            val intent = VideoActivityAutoBundle.builder(courseId, sectionId, itemId, video.id).build(it)
            startActivity(intent)
        }
    }

    override fun startCast(video: Video) {
        CastUtil.loadMedia(activity, video, true)
    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        inflater?.inflate(R.menu.refresh, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        val itemId = item!!.itemId
        when (itemId) {
            R.id.action_refresh -> {
                presenter.onRefresh()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onDestroyView() {
        super.onDestroyView()

        hdVideo.onDestroy()
        sdVideo.onDestroy()
        slides.onDestroy()
    }

    override fun getPresenterFactory(): PresenterFactory<VideoPreviewPresenter> {
        return VideoPreviewPresenterFactory(courseId, sectionId, itemId)
    }

}