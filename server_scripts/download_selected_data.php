<?php

if(($_SERVER['REQUEST_METHOD'] == 'GET') && isset($_GET['case_record_id'])) {
  require_once('db_connect.php');

  $caseRecordId = $_GET['case_record_id'];
  $urlDir = "http://128.199.205.226/getbetter/uploads";

  if($stmt = $mysqli->prepare("SELECT c.case_record_attachment_id,
    c.case_record_id, c.description, c.case_file_url,
    c.case_record_attachment_type_id, c.uploaded_on
    FROM tbl_case_record_attachments AS c
    LEFT JOIN tbl_users AS u ON c.uploaded_by = u.user_id
    WHERE u.role_id = 2 AND c.case_record_id = ?")) {

      $stmt->bind_param('i', $caseRecordId);

      $stmt->execute();

      $stmt->bind_result($case_record_attachment_id, $case_record_id, $description,
      $case_file_url, $case_record_attachment_type_id, $uploaded_on);

      $result = array();

      // $stmt->fetch();s
      while($stmt->fetch()) {

        $filePath = $urlDir . $case_file_url;
        array_push($result, array('case_attachment_id'=>$case_record_attachment_id,
        'case_record_id'=>$case_record_id,
        'description'=>$description,
        'file_path'=>$filePath,
        'case_attachment_type'=>$case_record_attachment_type_id,
        'uploaded_on'=>$uploaded_on));
      }

      $stmt->close();

      // var_dump($result);
      echo json_encode(array('case_attachments'=>$result));

    } else {
      echo 'SQL Query Fail';
    }


} else {
  echo 'Failed to run script';
}

$mysqli->close();
?>
