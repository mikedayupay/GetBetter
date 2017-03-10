<?php

/**
 *
 */
class Android_upload_case_record extends MY_Controller
{

  function index()
  {
    $format = 'Y-m-d H:i:s';
    $this->load->model('cases_model', 'cases');
    $this->load->model('case_records_model', 'case_records');

    $post = $this->input->post();
    // $_SESSION['user']->user_id = $post['case_record']['updated_by'];

    // echo $post['case_record']['control_number'];
    // var_dump($_FILES);

    $post['case_record'] = $this->common->clean_columns($this->case_records->columns, $post['case_record']);

    // print_r($post);

    if(!empty($post))
    {
      $post['case_record']['case_id'] = 6;
      $case_record = $this->case_records->create($post['case_record']);

      if($case_record)
      {
        $this->case_records->create_history(array(
          'case_record_id' => $case_record->case_record_id,
          'record_status_id' => 1,
          'updated_by' => 444, //$post['case_record']['updated_by'],
          'updated_on' => gmdate($format)
        ));

        echo $case_record->case_record_id;
      }
    }
    else
    {
      echo "post data is empty";
    }
  }
}
