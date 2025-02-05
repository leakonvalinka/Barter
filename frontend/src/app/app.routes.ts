import { Routes } from '@angular/router';
import { ComponentExamplesComponent } from './components/dev/component-examples/component-examples.component';
import { RegistrationComponent } from './components/pages/auth/registration/registration.component';
import { MainLayoutComponent } from './components/layouts/main-layout/main-layout.component';
import { AuthLayoutComponent } from './components/layouts/auth-layout/auth-layout.component';
import { UserProfileComponent } from './components/pages/user/profile-detail/user-profile.component';
import { LoginComponent } from './components/pages/auth/login/login.component';
import { ProfileEditComponent } from './components/pages/user/profile-edit/profile-edit.component';
import { ForgotPasswordComponent } from './components/pages/auth/forgot-password/forgot-password.component';
import { ResetPasswordComponent } from './components/pages/auth/reset-password/reset-password.component';
import { ownPageGuard } from './guards/own-page/own-page.guard';
import { ExploreComponent } from './components/pages/explore/explore.component';
import { SkillDetailComponent, SkillOfferDemandMode } from './components/pages/skills/skill-detail/skill-detail.component';
import { OfferEditComponent } from './components/pages/skills/offer-edit/offer-edit.component';
import { DemandComponent } from './components/pages/skills/demand/demand.component';
import { NotFoundComponent } from './components/pages/not-found/not-found.component';
import { RoleGuard } from './guards/roles/role.guard';
import { AuthGuard } from './guards/authentication/auth.guard';
import { VerifyComponent } from './components/pages/auth/verify/verify.component';
import { DemandEditComponent } from './components/pages/skills/demand-edit/demand-edit.component';
import { OfferComponent } from './components/pages/skills/offer/offer.component';
import { UserOnboardingComponent } from './components/pages/user/user-onboarding/user-onboarding.component';
import { UserReportsComponent } from './components/pages/admin/user-reports/user-reports.component';
import { PostReportsComponent } from './components/pages/admin/post-reports/post-reports.component';
import { RecommendComponent } from './components/pages/recommend/recommend.component';
import { ChatOverviewComponent } from './components/pages/chat/chat-overview/chat-overview.component';
import { ChatDetailComponent } from './components/pages/chat/chat-detail/chat-detail.component';
import { GuidelinesComponent } from './components/pages/guidelines/guidelines.component';

export const routes: Routes = [
  {
    path: '',
    component: MainLayoutComponent,
    children: [
      {
        path: '', redirectTo: '/explore', pathMatch: 'full'
      },
      // {
      //   path: 'dashboard', component: ComponentExamplesComponent, canActivate: [AuthGuard, RoleGuard],
      //   data: { roles: ['ADMIN', 'USER'] }
      // },
      {
        path: 'explore', component: ExploreComponent, canActivate: [AuthGuard, RoleGuard],
        data: { roles: ['ADMIN', 'USER'] }
      },
      {
        path: 'profile/:username', component: UserProfileComponent, canActivate: [AuthGuard, RoleGuard],
        data: { roles: ['ADMIN', 'USER'] }
      },
      {
        path: 'profile/edit/:username', component: ProfileEditComponent, canActivate: [AuthGuard, RoleGuard, ownPageGuard],
        data: { roles: ['ADMIN', 'USER'] }
      },
      {
        path: 'recommend', component: RecommendComponent, canActivate: [AuthGuard, RoleGuard],
        data: { roles: ['ADMIN', 'USER'] }
      },
      {
        path: 'demands',
        children: [
          {
            path: '',
            component: DemandComponent,
            canActivate: [AuthGuard, RoleGuard],
            data: { roles: ['ADMIN', 'USER'] }
          },
          {
            path: ':id',
            component: SkillDetailComponent,
            data: { roles: ['ADMIN', 'USER'], mode: SkillOfferDemandMode.demand }
          },
          {
            path: ':id/edit',
            component: DemandEditComponent,
            data: { roles: ['ADMIN', 'USER'] }
          }
        ]
      },
      {
        path: 'offers',
        children: [
          {
            path: '',
            component: OfferComponent,
            canActivate: [AuthGuard, RoleGuard],
            data: { roles: ['ADMIN', 'USER'] }
          },
          {
            path: ':id',
            component: SkillDetailComponent,
            data: { roles: ['ADMIN', 'USER'], mode: SkillOfferDemandMode.offer }
          },
          {
            path: ':id/edit',
            component: OfferEditComponent,
            data: { roles: ['ADMIN', 'USER'] }
          }
        ]
      },
      {
        path: 'admin',
        children: [
          {
            path: 'user-reports',
            component: UserReportsComponent,
            canActivate: [AuthGuard, RoleGuard],
            data: { roles: ['ADMIN'] }
          },
          {
            path: 'post-reports',
            component: PostReportsComponent,
            canActivate: [AuthGuard, RoleGuard],
            data: { roles: ['ADMIN'] }
          }
        ]
      },
      {
        path: 'chat',
        children: [
          {
            path: '',
            component: ChatOverviewComponent,
            canActivate: [AuthGuard, RoleGuard],
            data: { roles: ['ADMIN', 'USER'] }
          },
          {
            path: ':id',
            component: ChatDetailComponent,
            data: { roles: ['ADMIN', 'USER'] }
          },
        ]
      }
      // ... other routes that should have navigation
    ]
  },
  {
    path: '',
    component: AuthLayoutComponent,
    children: [
      {
        path: "onboarding",
        component: UserOnboardingComponent
      },

      { path: 'register', component: RegistrationComponent },
      { path: 'login', component: LoginComponent },
      { path: 'forgot-password', component: ForgotPasswordComponent },
      {
        path: 'reset-password', component: ResetPasswordComponent, canActivate: [AuthGuard],
        data: { roles: ['PASSWORD-RESET'] }
      },
      { path: 'verify', component: VerifyComponent },
      { path: 'guidelines', component: GuidelinesComponent },
      // ... other auth routes
    ]
  },
  // 404 Not Found route
  { path: '**', component: NotFoundComponent }
];
