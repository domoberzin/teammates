import { CommonModule } from '@angular/common';
import { Component, Input } from '@angular/core';
import {
  NgbDropdownModule,
  NgbTooltipModule,
} from '@ng-bootstrap/ng-bootstrap';
import { InstructorPermissionSet } from 'src/web/types/api-request';
import { JoinState } from '../../../types/api-output';
import { AjaxLoadingModule } from '../ajax-loading/ajax-loading.module';
import { SortableTableHeaderColorScheme } from '../sortable-table/sortable-table.component';
import { TeammatesRouterModule } from '../teammates-router/teammates-router.module';

@Component({
  selector: 'tm-group-buttons',
  templateUrl: './student-list-action-cell.component.html',
  standalone: true,
  imports: [
    CommonModule,
    TeammatesRouterModule,
    AjaxLoadingModule,
    NgbDropdownModule,
    NgbTooltipModule,
  ],
})
export class StudentListActionsComponent {
  @Input() idx: number = 0;
  @Input() fsName: string = '';
  @Input() courseId: string = '';
  @Input() email: string = '';
  @Input() hasJoined: boolean = false;
  @Input() isSendReminderLoading: boolean = false;
  @Input() enableRemindButton: boolean = false;
  @Input() isActionButtonsEnabled: boolean = true;

  @Input() instructorPrivileges: InstructorPermissionSet = {
    canModifyCourse: false,
    canModifyInstructor: false,
    canModifySession: false,
    canModifyStudent: false,
    canViewStudentInSections: false,
    canViewSessionInSections: false,
    canSubmitSessionInSections: false,
    canModifySessionCommentsInSections: false,
  };

  @Input() copySession: () => void = () => {};
  @Input() sendRemindersToAllNonSubmitters: () => void = () => {};
  @Input() sendRemindersToSelectedNonSubmitters: () => void = () => {};
  @Input() remindStudentFromCourse: () => void = () => {};
  @Input() removeStudentFromCourse : () => void = () => {};

  JoinState: typeof JoinState = JoinState;

  // enum
  SortableTableHeaderColorScheme: typeof SortableTableHeaderColorScheme =
    SortableTableHeaderColorScheme;
}
