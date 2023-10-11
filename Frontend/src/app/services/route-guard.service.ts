import { Injectable } from '@angular/core';
import { Router, ActivatedRouteSnapshot } from '@angular/router';
import { GlobalConstants } from '../shared/global-constants';
import { AuthService } from './auth.service';
import { SnackbarService } from './snackbar.service';
import jwt_decode from 'jwt-decode';

@Injectable({
  providedIn: 'root'
})
export class RouteGuardService {
  constructor(public auth:AuthService,
    public router:Router,
    private snackbarService:SnackbarService) { }

    canActivate(router:ActivatedRouteSnapshot):boolean{
      let expectRoleArray = router.data;
      expectRoleArray = expectRoleArray.expectedRole;

      const token:any = localStorage.getItem('token');

      var tokenPayload:any;

      try{
        tokenPayload = jwt_decode(token);
      }catch(err){
        localStorage.clear();
        this.router.navigate(['/']);
      }

      let expectedRole = '';

      for(let i = 0 ;  i < expectRoleArray.length; i++){
        if(expectRoleArray[i] == tokenPayload.role){
          expectedRole = tokenPayload.role;
        }
      }


      if(tokenPayload.role == 'user' || tokenPayload.role == 'admin'){
        return true;
        if(this.auth.isAuthenticated() && tokenPayload.role == expectedRole){
          return true;
        }
        
        this.snackbarService.openSnackBar(GlobalConstants.unauthroized , GlobalConstants.error);
        this.router.navigate(['/eats-hub/dashboard']);
        return false;
      }
      else{
        this.router.navigate(['/']);  
        localStorage.clear();
        return false;
      }
    }
}