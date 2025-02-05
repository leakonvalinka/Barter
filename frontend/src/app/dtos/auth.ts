export interface LoginResponseDTO {
  jwt: string;
  refreshToken: string;
  firstLogin: boolean;
}
