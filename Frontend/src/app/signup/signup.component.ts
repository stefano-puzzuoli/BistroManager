import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { Route, Router } from '@angular/router';
import { UserService } from '../services/user.service';
import { SnackbarService } from '../services/snackbar.service';
import { MatDialogRef } from '@angular/material/dialog';
import { NgxUiLoaderService } from 'ngx-ui-loader';
import { GlobalConstants } from '../shared/global-constants';

@Component({
  selector: 'app-signup',
  templateUrl: './signup.component.html',
  styleUrls: ['./signup.component.scss']
})
export class SignupComponent implements OnInit {
  password =  true;
  confirmPassword = true;
  signupForm:any = FormGroup;
  responseMessage:any;

  constructor(private formBuilder:FormBuilder,
    private router:Router,
    private userService:UserService,
    private snackbarService:SnackbarService,
    public dialogReference:MatDialogRef<SignupComponent>,
    private ngxService:NgxUiLoaderService
    ) { }

  ngOnInit(): void {
    this.signupForm = this.formBuilder.group({
      name:[null, [Validators.required, Validators.pattern(GlobalConstants.nameRegex)]],
      email:[null, [Validators.required, Validators.pattern(GlobalConstants.emailRegex)]],
      contactNumber:[null, [Validators.required, Validators.pattern(GlobalConstants.contactNumberRegex)]],
      password: [null, [Validators.required]],
      confirmPassword: [null, [Validators.required]]
  })
  }

  validateSubmit(){
    if(this.signupForm.controls['password'].value != this.signupForm.controls['confirmPassword'].value) {
      return true;
    } else {
      return false;
    }
  }

  handleSubmit(){
    this.ngxService.start();
    var formDate = this.signupForm.value;
    var data = {
      name: formDate.name,
      email: formDate.email,
      contactNumber: formDate.contactNumber,
      password: formDate.password,
    }
    this.userService.signup(data).subscribe((response:any)=>{
      this.ngxService.stop();
      this.dialogReference.close();
      this.responseMessage = response?.message;
      this.snackbarService.openSnackBar(this.responseMessage,"");
      // alert("Successfully Login");
      this.router.navigate(['/food-hub/login']);
    },(error: { error: { message: any; }; })=>{
      this.ngxService.stop();
      if(error.error?.message){
        this.responseMessage = error.error?.message;
      }else{
        this.responseMessage = GlobalConstants.genericError;
      }
      // alert(this.responseMessage +" " +GlobalConstants.error);
      this.snackbarService.openSnackBar(this.responseMessage , GlobalConstants.error);
    })
  }

}
