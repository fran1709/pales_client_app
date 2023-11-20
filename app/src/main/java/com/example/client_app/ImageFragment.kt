import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.fragment.app.Fragment
import com.example.client_app.R


class ImageFragment : Fragment() {

    private var imageResourceId: Int = 0

    companion object {
        fun newInstance(imageResourceId: Int): ImageFragment {
            val fragment = ImageFragment()
            val args = Bundle()
            args.putInt("imageResourceId", imageResourceId)
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            imageResourceId = it.getInt("imageResourceId")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val rootView = inflater.inflate(R.layout.fragment_carrusel, container, false)
        val imageView = rootView.findViewById<ImageView>(R.id.imageView)

        imageView.setImageResource(imageResourceId)

        return rootView
    }
}
