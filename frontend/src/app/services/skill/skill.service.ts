import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import {
  CreateDemand,
  CreateOffer,
  Skill,
  SkillCategory,
  SkillDemand,
  SkillDetail,
  SkillOffer,
  UpdateSkillDemand, UpdateSkillOffer
} from '../../dtos/skill';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { PaginatedResults, PaginationParams, paginationToHttpQueryParams } from '../../dtos/pagination';

@Injectable({
  providedIn: 'root'
})
export class SkillService {

  private skillEndpointUrl = `${environment.apiBaseUrl}/skills`;
  private categoryEndpointUrl = `${environment.apiBaseUrl}/skills/categories`;
  private skillOfferEndpointUrl = `${environment.apiBaseUrl}/skills/offer`;
  private skillDemandEndpointUrl = `${environment.apiBaseUrl}/skills/demand`;
  private recommendationEndpointUrl = `${environment.apiBaseUrl}/recommendation`;

  constructor(private http: HttpClient) { }

  /**
   * fetches a specified user's skills
   * @param userId specifies the user
   */
  getUserSkills(userId: string): Observable<Skill[]> {
    return this.http.get<any>(this.skillEndpointUrl + "/" + userId);
  }

  /**
   * updates a specified users skills
   * @param updatedSkills the updated skills
   * @param userId specifies the user
   */
  updateUserSkills(updatedSkills: Skill[], userId: string): Observable<any> {
    return this.http.put(this.skillEndpointUrl + "/" + userId, updatedSkills);
  }

  /**
   * gets a generic skill by id
   * @param skillId
   */
  getSkillById(skillId: number): Observable<Skill> {
    return this.http.get<Skill>(this.skillEndpointUrl + "/" + skillId);
  }

  /**
   * Updates a demand skill.
   * @param skill The skill to update.
   */
  updateDemand(skill: UpdateSkillDemand): Observable<Skill> {
    return this.http.put<Skill>(this.skillDemandEndpointUrl + "/" + skill.id, skill);
  }

  /**
   * Updates a skill.
   * @param skill The skill to update.
   */
  updateOffer(skill: UpdateSkillOffer): Observable<SkillOffer> {
    return this.http.put<SkillOffer>(this.skillOfferEndpointUrl + "/" + skill.id, skill);
  }

  /**
   * gets a single demand skill by id
   * @param skillId
   */
  getSkillDemandById(skillId: number): Observable<SkillDemand> {
    return this.http.get<SkillDemand>(this.skillDemandEndpointUrl + "/" + skillId);
  }

  /**
   * gets a single offer skill by id
   * @param skillId
   */
  getSkillOfferById(skillId: number): Observable<SkillOffer> {
    return this.http.get<SkillOffer>(this.skillOfferEndpointUrl + "/" + skillId);
  }

  /**
   * Updates a skill.
   * @param skill The skill to update.
   */
  updateSkill(skill: Skill): Observable<Skill> {
    return this.http.post<Skill>(this.skillEndpointUrl, skill);
  }

  /**
   * Fetches a skill category by ID.
   * @param categoryId The category ID.
   */
  getSkillCategoryById(categoryId: number): Observable<SkillCategory> {
    return this.http.get<SkillCategory>(`${this.categoryEndpointUrl}/${categoryId}`);
  }

  /**
   * Fetches all skill categories.
   */
  getSkillCategories(): Observable<SkillCategory[]> {
    return this.http.get<SkillCategory[]>(this.categoryEndpointUrl);
  }

  /**
   * creates a new demand
   * @param demand the demand to be created
   */
  createDemand(demand: CreateDemand): Observable<any> {
    return this.http.post<Observable<CreateDemand>>(this.skillDemandEndpointUrl, demand);
  }

  /**
   * Fetches skill demands or offers based on search criteria
   * @param endpoint - the endpoint URL to use (demand or offer)
   * @param categories - array of category IDs
   * @param lat - latitude coordinate
   * @param lon - longitude coordinate
   * @param paginationParams - pagination parameters (page number, number of elements on a page)
   * @param radius - search radius in kilometers
   */
  private getSkillListings(
    endpoint: string,
    categories?: number[],
    lat?: number,
    lon?: number,
    radius?: number,
    paginationParams: PaginationParams = { page: 0, pageSize: 50 },
  ): Observable<PaginatedResults<SkillDetail>> {
    let params = paginationToHttpQueryParams(paginationParams);

    if (categories?.length) {
      categories.forEach(cat => {
        params = params.append('category', cat.toString());
      });
    }
    if (lat !== undefined) params = params.append('lat', lat.toString());
    if (lon !== undefined) params = params.append('lon', lon.toString());
    if (radius !== undefined) params = params.append('radius', radius.toString());

    return this.http.get<PaginatedResults<SkillDetail>>(endpoint, { params });
  }

  /**
   * Fetches skill demands based on search criteria
   */
  getDemands(
    categories?: number[],
    lat?: number,
    lon?: number,
    radius?: number,
    paginationParams?: PaginationParams
  ) {
    return this.getSkillListings(
      this.skillDemandEndpointUrl,
      categories,
      lat,
      lon,
      radius,
      paginationParams
    );
  }

  /**
   * Fetches skill offers based on search criteria
   */
  getOffers(
    categories?: number[],
    lat?: number,
    lon?: number,
    radius?: number,
    paginationParams?: PaginationParams
  ) {
    return this.getSkillListings(
      this.skillOfferEndpointUrl,
      categories,
      lat,
      lon,
      radius,
      paginationParams
    );
  }

  /*
   * creates a new demand
   * @param offer the demand to be created
   */
  createOffer(offer: CreateOffer): Observable<any> {
    return this.http.post<Observable<CreateDemand>>(this.skillOfferEndpointUrl, offer);
  }

  deleteSkill(id: number): Observable<any> {
    return this.http.delete<Observable<Response>>(this.skillEndpointUrl + "/" + id);
  }

  /**
   * Fetches skill demands or offers based on search criteria
   * @param endpoint - the endpoint URL to use (demand or offer)
   * @param paginationParams - pagination parameters (page number, number of elements on a page)
   */
  getRecommendations(
    demand: SkillDemand
    // paginationParams: PaginationParams = {page: 0, pageSize: 50},
  ): Observable<PaginatedResults<SkillDetail>> {
    // let params = paginationToHttpQueryParams(paginationParams);
    console.log("Requesting recommendations for demand: " + demand.title);
    return this.http.post<PaginatedResults<SkillDetail>>(this.recommendationEndpointUrl, demand);
  }
}
