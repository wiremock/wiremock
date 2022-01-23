import * as joint from 'jointjs';
import {dia} from 'jointjs';
import Element = dia.Element;
import {StateMappingInfoComponent} from '../state-mapping-info/state-mapping-info.component';
import {NgbModal} from '@ng-bootstrap/ng-bootstrap';
import {StubMapping} from '../../model/wiremock/stub-mapping';

export class StateMachineItems {

  public static createInfoButton(modalService: NgbModal, mapping: StubMapping) {
    (joint.linkTools as any).InfoButton = joint.linkTools.Button.extend({
      name: 'info-button',
      options: {
        markup: [ {
          tagName: 'circle',
          selector: 'button',
          attributes: {
            'r': 7,
            'fill': '#f58e00',
            'cursor': 'pointer'
          }
        }, {
          tagName: 'path',
          selector: 'icon',
          attributes: {
            'd': 'M -2 4 2 4 M 0 3 0 0 M -2 -1 1 -1 M -1 -4 1 -4',
            'fill': 'none',
            'stroke': '#FFFFFF',
            'stroke-width': 2,
            'pointer-events': 'none'
          }
        } ],
        distance: '50%',
        offset: 0,
        action: function (evt) {
          const modalRef = modalService.open(StateMappingInfoComponent, {
            size: 'lg',
            windowClass: 'modal-h70'
          });
          modalRef.componentInstance.mapping = mapping;
        }
      }
    });

    const infoButton = new (joint.linkTools as any).InfoButton();

    return new joint.dia.ToolsView({
      tools: [ infoButton ]
    });
  }

  public static createSelfState(): Element {
    const circle = new joint.shapes.standard.Circle();
    circle.resize(1, 1);
    circle.attr({
      body: {
        fill: 'black'
      }
    });
    return circle;
  }

  public static createActiveStartState(): Element {
    return StateMachineItems.createStartState('#F58E00');
  }

  public static createStartState(color: string = 'black'): Element {
    const circle = new joint.shapes.standard.Circle();
    circle.resize(20, 20);
    circle.attr({
      label: {
        textWrap: {
          text: 'Started',
          height: '50%',
          ellipsis: true,
          width: -20
        },
        title: 'Started'
      },
      body: {
        fill: color,
        cursor: 'move'
      }
    });
    return circle;
  }

  public static createAnyState(): Element {
    const ellipse = new joint.shapes.standard.Ellipse();
    ellipse.resize(60, 60);
    ellipse.attr({
      label: {
        textWrap: {
          text: 'ANY',
          height: '50%',
          ellipsis: true,
          width: -20
        },
        title: 'ANY'
      },
      body: {
        fill: '#29ABE2',
        cursor: 'move'
      }
    });
    return ellipse;
  }

  public static createActiveState(title: string): Element {
    return StateMachineItems.createStatePrivate(title, '#F58E00', '#FFFFFF');
  }

  public static createState(title: string): Element {
    return StateMachineItems.createStatePrivate(title, '#FFE6C9', '#000000');
  }

  public static createStatePrivate(title: string, bgColor: string, color: string): Element {
    const ellipse = new joint.shapes.standard.Ellipse();
    ellipse.resize(160, 100);
    ellipse.attr({
      label: {
        textWrap: {
          text: title,
          height: '50%',
          ellipsis: true,
          width: -20,
        },
        title: title,
        color: color,
        fill: color
      },
      body: {
        fill: bgColor,
        cursor: 'move'
      }
    });
    return ellipse;
  }
}
