package com.fdl.storyapp.ui.poststory

import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupWindow
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.fdl.storyapp.R
import com.fdl.storyapp.databinding.FragmentPostStoryBinding
import com.fdl.storyapp.databinding.PopupChooseImageSourceBinding
import com.fdl.storyapp.model.UserModel
import com.fdl.storyapp.ui.CameraActivity
import com.fdl.storyapp.ui.MainActivity
import com.fdl.storyapp.utilities.*
import com.fdl.storyapp.utilities.Utils.reduceFileImage
import com.fdl.storyapp.utilities.Utils.rotateBitmap
import com.fdl.storyapp.utilities.Utils.showToast
import com.fdl.storyapp.utilities.Utils.uriToFile
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.BufferedOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream


class PostStoryFragment : Fragment() {

    private var _binding: FragmentPostStoryBinding? = null
    private val binding get() = _binding!!
    private lateinit var user: UserModel

    private val viewModel: PostStoryViewModel by viewModels {
        ViewModelFactory.getInstance(requireContext())
    }

    private var currentFile: File? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (!checkAllPermissionsGranted()) {
            ActivityCompat.requestPermissions(
                requireActivity(),
                MainActivity.REQUIRED_PERMISSIONS,
                MainActivity.REQUEST_CODE_PERMISSIONS
            )
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPostStoryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupView()
        observeViewModel()
    }

    private fun setupView() {
        binding.btnBack.setOnClickListener {
            findNavController().navigateUp()
        }

        binding.btnPost.setOnClickListener {
            uploadFile()
        }

        setupPopupMenu()
    }

    private fun observeViewModel() {
        viewModel.userModel.observe(viewLifecycleOwner) { userModel ->
            this.user = userModel
        }

        viewModel.isSuccess.observe(viewLifecycleOwner) { isSuccess ->
            if (isSuccess) {
                findNavController().navigateUp()
            }
        }

        viewModel.errorMessage.observe(viewLifecycleOwner) { message ->
            showToast(requireContext(), message)
        }

        viewModel.isLoading.observe(viewLifecycleOwner) { state ->
            showLoading(state)
        }
    }

    private fun setupPopupMenu() {
        val popupBinding = PopupChooseImageSourceBinding.inflate(layoutInflater)
        val popupWindow = PopupWindow(
            popupBinding.root,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        ).apply {
            isFocusable = true
            elevation = 10F
        }

        popupBinding.btnCamera.setOnClickListener {
            startCameraX()
            popupWindow.dismiss()
        }

        popupBinding.btnGallery.setOnClickListener {
            startGallery()
            popupWindow.dismiss()
        }

        binding.btnChangeImage.setOnClickListener { btn ->
            popupWindow.showAsDropDown(btn)
        }
    }

    private fun uploadFile() {
        val description = binding.etCaption.text.toString().trim()

        if (description.isEmpty()) {
            binding.etCaption.error = getString(R.string.must_not_be_empty)
            return
        }

        if (currentFile != null) {
            val file = reduceFileImage(currentFile as File)

            val desc = description.toRequestBody("text/plain".toMediaType())
            val requestImageFile = file.asRequestBody("image/jpeg".toMediaTypeOrNull())
            val imageMultipart: MultipartBody.Part = MultipartBody.Part.createFormData(
                "photo",
                file.name,
                requestImageFile
            )

            viewModel.uploadImage(user.token ?: "", imageMultipart, desc)

        } else {
            showToast(requireContext(), getString(R.string.choose_image_method))
        }
    }

    private fun checkAllPermissionsGranted() = MainActivity.REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(
            requireActivity().baseContext,
            it
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun startCameraX() {
        launcherIntentCameraX.launch(Intent(requireContext(), CameraActivity::class.java))
    }

    private fun startGallery() {
        val intent = Intent().apply {
            action = Intent.ACTION_GET_CONTENT
            type = "image/*"
        }
        launcherIntentGallery.launch(Intent.createChooser(intent, "Choose a picture!"))
    }

    private val launcherIntentGallery =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode == AppCompatActivity.RESULT_OK) {
                val selectedImage = it.data?.data as Uri
                val myFile = uriToFile(selectedImage, requireContext())
                currentFile = myFile
                binding.ivPreview.setImageURI(selectedImage)
            }
        }

    private val launcherIntentCameraX = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        if (it.resultCode == MainActivity.CAMERA_X_RESULT) {
            val myFile = it.data?.getSerializableExtra("picture") as File
            val isBackCamera = it.data?.getBooleanExtra("isBackCamera", true) as Boolean
            val result = rotateBitmap(BitmapFactory.decodeFile(myFile.path), isBackCamera)

            val os: OutputStream = BufferedOutputStream(FileOutputStream(myFile))
            result.compress(Bitmap.CompressFormat.JPEG, 100, os)
            os.close()

            currentFile = myFile

            binding.ivPreview.setImageBitmap(result)
        }
    }

    private fun showLoading(isLoading: Boolean) {
        binding.loadingProgressBar.visibility = if (isLoading) View.VISIBLE else View.INVISIBLE
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}