<?php

/**
 *
 */
class Android_upload_attachments extends MY_Controller
{

  function index()
  {
    $format = 'Y-m-d H:i:s';
    $this->load->model('case_records_model', 'case_records');

    $post = $this->input->post();
    $_SESSION['user']->user_id = 444;

    if(!empty($post))
    {
      $user_directory = $this->common->user_directory . $post['attachment']['user_id'] . $this->common->directory_separator . 'case_records';
      if (!file_exists($user_directory))
      {
        if (mkdir($user_directory, true))
        {
            chmod($user_directory, 0777);
        }
      }
      $user_directory .= $this->common->directory_separator . $post['attachment']['case_record_id'];

      if (!file_exists($user_directory))
      {
        if (mkdir($user_directory, true))
        {
          chmod($user_directory, 0777);
        }
      }
      $user_directory .= $this->common->directory_separator;

      $config['upload_path'] = $user_directory;
      $config['allowed_types'] = '*';
      $this->load->library('upload');

      $config['file_name'] = urlencode(sha1(microtime()) . '-' . str_replace(' ','', $_FILES['attachmentFile']['name']));
      $this->upload->initialize($config);

      if($this->upload->do_upload('attachmentFile'))
      {
        $post['attachment']['case_file_url'] = $post['attachment']['user_id'] . $this->common->directory_separator . 'case_records' . $this->common->directory_separator . $post['attachment']['case_record_id'] . $this->common->directory_separator . $config['file_name'];
        $post['attachment']['uploaded_on'] = gmdate($format);
        // print_r($post['attachments'][$file_key]);
        $this->case_records->create_attachment($post['attachment']);
        echo $post;
      }
    }
    else
    {
      echo "post data empty";
    }
  }
}
