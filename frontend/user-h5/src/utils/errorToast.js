import { showToast } from 'vant'

/**
 * Red top-aligned error notice used by the H5 app.
 * Keeps API failures visually consistent with the admin's red message.
 */
export function showTopError(message) {
  showToast({
    type: 'fail',
    position: 'top',
    message,
    duration: 3200,
    className: 'user-error-toast'
  })
}
