# Webhook Setup Guide

## Overview
This document explains how changes pushed to this repository can automatically trigger updates on your server through webhooks.

## How It Works

### Push Requests
**YES**, any update made to this repository counts as a push request when committed and pushed to GitHub. This includes:
- Direct commits to branches
- Merged pull requests
- Updates from automated tools or agents

### Automatic Deployment Flow

1. **Code Update** → Developer or automated agent pushes changes to GitHub
2. **GitHub Actions Triggered** → The workflow in `.github/workflows/deploy.yml` runs automatically
3. **Webhook Notification** → GitHub sends a webhook payload to your configured server
4. **Server Deployment** → Your server receives the webhook and deploys the updated code

## Setting Up GitHub Webhooks

### Method 1: Using GitHub Repository Webhooks

1. Go to your repository on GitHub: `https://github.com/sanjaymishra2002/lifecare`
2. Click on **Settings** → **Webhooks** → **Add webhook**
3. Configure the webhook:
   - **Payload URL**: Your server endpoint (e.g., `https://your-server.com/webhook`)
   - **Content type**: `application/json`
   - **Secret**: Add a secret token for security (optional but recommended)
   - **Events**: Select "Just the push event" or customize as needed
   - **Active**: Check this box
4. Click **Add webhook**

### Method 2: Using GitHub Actions Workflow

The workflow file `.github/workflows/deploy.yml` includes a step for sending webhooks programmatically:

1. Open `.github/workflows/deploy.yml`
2. Locate the "Webhook deployment" step
3. Uncomment the `curl` command
4. Replace `YOUR_WEBHOOK_URL` with your actual server endpoint
5. Commit and push the changes

Example webhook payload that will be sent:
```json
{
  "repository": "sanjaymishra2002/lifecare",
  "branch": "main",
  "commit": "abc123...",
  "pusher": "username",
  "message": "commit message"
}
```

## Server-Side Configuration

Your server needs to:

1. **Listen for webhook requests** at the configured endpoint
2. **Verify the webhook** (if using a secret token)
3. **Pull latest changes** from the repository
4. **Deploy/restart** your application

### Example Server Handler (Node.js)

```javascript
const express = require('express');
const crypto = require('crypto');
const { exec } = require('child_process');

const app = express();
app.use(express.json());

const WEBHOOK_SECRET = 'your-secret-token';

app.post('/webhook', (req, res) => {
  // Verify signature if using secret
  const signature = req.headers['x-hub-signature-256'];
  if (signature) {
    const hash = crypto
      .createHmac('sha256', WEBHOOK_SECRET)
      .update(JSON.stringify(req.body))
      .digest('hex');
    
    if (`sha256=${hash}` !== signature) {
      return res.status(401).send('Invalid signature');
    }
  }
  
  // Pull latest changes and deploy
  exec('cd /path/to/your/app && git pull origin main && ./deploy.sh', (error, stdout, stderr) => {
    if (error) {
      console.error(`Error: ${error}`);
      return res.status(500).send('Deployment failed');
    }
    console.log(`Output: ${stdout}`);
    res.status(200).send('Deployment successful');
  });
});

app.listen(3000, () => console.log('Webhook server running on port 3000'));
```

### Example Server Handler (Python Flask)

```python
from flask import Flask, request, jsonify
import hmac
import hashlib
import subprocess

app = Flask(__name__)
WEBHOOK_SECRET = 'your-secret-token'

@app.route('/webhook', methods=['POST'])
def webhook():
    # Verify signature if using secret
    signature = request.headers.get('X-Hub-Signature-256')
    if signature:
        hash_obj = hmac.new(
            WEBHOOK_SECRET.encode(),
            request.data,
            hashlib.sha256
        )
        expected_signature = 'sha256=' + hash_obj.hexdigest()
        
        if not hmac.compare_digest(signature, expected_signature):
            return jsonify({'error': 'Invalid signature'}), 401
    
    # Pull latest changes and deploy
    try:
        subprocess.run(
            ['sh', '-c', 'cd /path/to/your/app && git pull origin main && ./deploy.sh'],
            check=True
        )
        return jsonify({'status': 'success'}), 200
    except subprocess.CalledProcessError:
        return jsonify({'error': 'Deployment failed'}), 500

if __name__ == '__main__':
    app.run(host='0.0.0.0', port=3000)
```

## Testing the Setup

1. Make a small change to your repository
2. Commit and push the change:
   ```bash
   git add .
   git commit -m "Test webhook deployment"
   git push origin main
   ```
3. Check GitHub Actions tab to see if the workflow ran successfully
4. Check your server logs to verify the webhook was received
5. Verify that your application was updated

## Security Best Practices

1. **Use HTTPS** for your webhook endpoint
2. **Validate webhook signatures** using a secret token
3. **Limit webhook permissions** - only pull and deploy, don't expose sensitive operations
4. **Log all webhook events** for auditing
5. **Rate limit** webhook endpoints to prevent abuse
6. **Use IP whitelisting** if possible (GitHub webhook IPs are documented)

## Troubleshooting

### Webhook not triggering
- Verify the webhook is active in GitHub settings
- Check the webhook delivery history in GitHub
- Ensure your server is accessible from the internet
- Check firewall settings

### Deployment fails
- Verify git credentials on your server
- Check file permissions
- Review server logs for error messages
- Ensure all dependencies are installed

### Build fails in GitHub Actions
- Check the Actions tab for error logs
- Verify all required secrets/environment variables are set
- Ensure `google-services.json` is properly configured

## Additional Resources

- [GitHub Webhooks Documentation](https://docs.github.com/en/webhooks)
- [GitHub Actions Documentation](https://docs.github.com/en/actions)
- [Webhook Security Best Practices](https://docs.github.com/en/webhooks/using-webhooks/best-practices-for-using-webhooks)

## Support

For issues with the webhook setup, please:
1. Check the GitHub Actions logs
2. Review your server logs
3. Verify webhook configuration in GitHub repository settings
4. Test the webhook manually using the "Redeliver" option in GitHub
