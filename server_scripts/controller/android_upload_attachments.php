<?php

/**
 *
 */
class Android_upload_attachments extends MY_Controller
{

  function index()
  {
    $format = 'Y-m-d H:i:s';
    $post = $this->input->post();

    $user_directory = $this->common->user_directory . $post['case_record_id'] . $this->common->directory_separator . 'case_records';
    if (!file_exists($user_directory))
    {
      if (mkdir($user_directory))
      {
          chmod($user_directory, 0777);
      }
    }
    $user_directory .= $this->common->directory_separator . $post['case_record_id'];

    if (!file_exists($user_directory))
    {
      if (mkdir($user_directory))
      {
        chmod($user_directory, 0777);
      }
    }
    $user_directory .= $this->common->directory_separator;

    $config['upload_path'] = $user_directory;
    $config['allowed_types'] = '*';
    $this->load->library('upload');

    $config['file_name'] = urlencode(sha1(microtime()) . '-' . $_FILES['attachmentFile']['name']);
    $this->upload->initialize($config);

    if($this->upload->do_upload('attachmentFile'))
    {
      $post['case_file_url'] = $post['case_record']['user_id'] . '//case_records//' . $post['case_record_id'] . '/' . $config['file_name'];
      $post['uploaded_on'] = gmdate($format);
      // print_r($post['attachments'][$file_key]);
      $this->case_records->create_attachment($post);
    }

  }
}
