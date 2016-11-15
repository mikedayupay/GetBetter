<?php

/**
 *
 */

class Android_upload_patients extends MY_Controller
{

  function index()
  {
    echo "Hello world!";
    $this->load->model('users_model', 'users');
    $post = $this->input->post();

        if (!empty($post))
        {
          foreach ($post as $patient)
          {
            $patient = $this->common->clean_columns(array_merge($this->users->columns, array (
                    'home',
                    'office',
                    'mobile',
                    'update_only',
                    'dialog_view'
                )), $patient);


            $field['role_id'] = 6;
            if (empty($patient['civil_status_id']))
            {
              unset($patient['civil_status_id']);
            }

            if (!isset($patient['user_id']))
            {
              $user = $this->users->create($patient);
            }
            else
            {
              $user = $this->users->update($patient);
            }

            if ($user)
            {
              $user_directory = $this->common->user_directory . $user->user_id;
              if (!file_exists($user_directory))
              {
                  if (mkdir($user_directory))
                  {
                      chmod($user_directory, 0777);
                  }
              }

              $profile_image = $_FILES[$user->user_id]['tmp_name'];

              $config['upload_path'] = $user_directory;
              $config['allowed_types'] = 'jpg|jpeg|png';
              $config['overwrite'] = true;
              $config['file_name'] = $_FILES[$user->user_id]['name'];
              $this->load->library('upload', $config);

              $user_directory .= $this->common->directory_separator;
              if ($this->upload->do_upload($profile_image))
              {
                  if ($user->logo && file_exists($user->profile_url))
                  {
                      unlink($user->profile_url);
                  }
                  $this->users->update(array(
                      'user_id' => $user->user_id,
                      'profile_url' => 'uploads/' . $user->user_id . '/' . $file_name
                  ));
              }

              $patient['home']['user_id'] = $user->user_id;
              $this->users->create_home_address($patient['home']);
              $this->users->create_home_contact($patient['home']);

              if (isset($patient['office']))
              {
                  $patient['office']['user_id'] = $user->user_id;
                  $this->users->create_office_address($patient['office']);
                  $this->users->create_office_contact($patient['office']);
              }
              if (isset($patient['mobile']))
              {
                  $patient['mobile']['user_id'] = $user->user_id;
                  $this->users->create_mobile_contact($patient['mobile']);
              }

          }
          else
          {
            echo "failed to upload";
          }
##################################################################
            // $post['role_id'] = 6;
            // if (empty($post['civil_status_id']))
            // {
            //     unset($post['civil_status_id']);
            // }
            //
            // if (!isset($post['user_id']))
            // {
            //     $user = $this->users->create($post);
            // }
            // else
            // {
            //     $user = $this->users->update($post);
            // }
            // if ($user)
            // {
            //     $user_directory = $this->common->user_directory . $user->user_id;
            //     if (!file_exists($user_directory))
            //     {
            //         if (mkdir($user_directory))
            //         {
            //             chmod($user_directory, 0777);
            //         }
            //     }
            //
            //     $file_name = $post['first_name'].'_'.$post['last_name'].'.jpg';
            //     $config['upload_path'] = $user_directory;
            //     $config['allowed_types'] = 'jpg|jpeg|png';
            //     $config['overwrite'] = true;
            //     $config['file_name'] = $file_name;
            //     $this->load->library('upload', $config);
            //
            //     $user_directory .= $this->common->directory_separator;
            //     if ($this->upload->do_upload('profile_url'))
            //     {
            //         if ($user->logo && file_exists($user->profile_url))
            //         {
            //             unlink($user->profile_url);
            //         }
            //         $this->users->update(array(
            //             'user_id' => $user->user_id,
            //             'profile_url' => 'uploads/' . $user->user_id . '/' . $file_name;
            //         ));
            //     }
            //
            //     $post['home']['user_id'] = $user->user_id;
            //     $this->users->create_home_address($post['home']);
            //     $this->users->create_home_contact($post['home']);
            //     if (isset($post['office']))
            //     {
            //         $post['office']['user_id'] = $user->user_id;
            //         $this->users->create_office_address($post['office']);
            //         $this->users->create_office_contact($post['office']);
            //     }
            //     if (isset($post['mobile']))
            //     {
            //         $post['mobile']['user_id'] = $user->user_id;
            //         $this->users->create_mobile_contact($post['mobile']);
            //     }
            // }
          }
        }
        else
        {
          echo 'post data empty';
        }
    }
}
